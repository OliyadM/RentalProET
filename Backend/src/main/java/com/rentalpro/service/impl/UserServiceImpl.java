package com.rentalpro.service.impl;

import com.rentalpro.model.dto.request.ProfileUpdateRequest;
import com.rentalpro.model.dto.request.ProfileVerificationRequest;
import com.rentalpro.model.dto.request.RegisterRequest;
import com.rentalpro.model.dto.response.ProfileResponse;
import com.rentalpro.model.entity.User;
import com.rentalpro.model.enums.AccountStatus;
import com.rentalpro.model.enums.EntityType;
import com.rentalpro.model.enums.NotificationType;
import com.rentalpro.repository.UserRepository;
import com.rentalpro.service.NotificationService;
import com.rentalpro.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .subCityZone(request.getSubCityZone())
                .isActive(true)
                .accountStatus(AccountStatus.PENDING_PROFILE)
                .entityType(EntityType.INDIVIDUAL)
                .build();

        // saveAndFlush ensures the INSERT is sent to the DB immediately so that
        // the subsequent notificationService.send() can find this user by ID
        // within the same transaction without hitting a constraint or stale-read.
        user = userRepository.saveAndFlush(user);

        // REQ-1: Welcome notification — wrapped so failure never blocks registration
        try {
            notificationService.send(
                    user.getId(),
                    NotificationType.ACCOUNT_CREATED,
                    "Welcome to RentalPro ET! Your account has been created. " +
                    "Please complete your profile to get started.",
                    user.getId());
        } catch (Exception e) {
            log.warn("Failed to send ACCOUNT_CREATED notification to user {}: {}", user.getId(), e.getMessage());
        }

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate business fields if entity type is BUSINESS
        if (request.getEntityType() == EntityType.BUSINESS) {
            if (request.getBusinessRegNumber() == null || request.getBusinessRegNumber().trim().isEmpty()) {
                throw new RuntimeException("Business registration number is required for business entities");
            }
        }

        // Check for duplicate national ID
        if (request.getNationalIdNumber() != null && 
            !request.getNationalIdNumber().equals(user.getNationalIdNumber())) {
            userRepository.findByNationalIdNumber(request.getNationalIdNumber())
                    .ifPresent(u -> {
                        throw new RuntimeException("National ID number already registered");
                    });
        }

        // Check for duplicate TIN
        if (request.getTinNumber() != null && 
            !request.getTinNumber().equals(user.getTinNumber())) {
            userRepository.findByTinNumber(request.getTinNumber())
                    .ifPresent(u -> {
                        throw new RuntimeException("TIN number already registered");
                    });
        }

        // Update profile fields
        user.setDateOfBirth(request.getDateOfBirth());
        user.setResidentialAddress(request.getResidentialAddress());
        user.setNationalIdNumber(request.getNationalIdNumber());
        user.setNationalIdDocumentUrl(request.getNationalIdDocumentUrl());
        user.setTinNumber(request.getTinNumber());
        user.setEntityType(request.getEntityType());
        user.setBusinessRegNumber(request.getBusinessRegNumber());
        user.setBusinessRegDocumentUrl(request.getBusinessRegDocumentUrl());

        // Update status to PENDING_VERIFICATION if profile is complete
        if (isProfileComplete(user)) {
            user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
            // Must flush before sending notification so the updated status
            // is visible to the REQUIRES_NEW notification transaction.
            userRepository.saveAndFlush(user);

            // REQ-2: Fan-out to officers in the user's sub-city
            String subCity = user.getSubCityZone();
            if (subCity != null && !subCity.isBlank()) {
                try {
                    String roleLabel = user.getRole().name().charAt(0)
                            + user.getRole().name().substring(1).toLowerCase().replace("_", " ");
                    String msg = String.format(
                            "%s %s (%s) has submitted their profile for verification in %s.",
                            user.getFirstName(), user.getLastName(), roleLabel, subCity);
                    notificationService.sendToSubCityOfficers(
                            subCity,
                            NotificationType.PROFILE_PENDING_REVIEW,
                            msg,
                            user.getId());
                } catch (Exception e) {
                    log.warn("Failed to send PROFILE_PENDING_REVIEW notification for user {}: {}",
                            user.getId(), e.getMessage());
                }
            } else {
                log.warn("User {} has no subCityZone — PROFILE_PENDING_REVIEW notification skipped. " +
                         "Assign a sub-city to this user so officers can be notified.", user.getId());
            }
        } else {
            userRepository.save(user);
        }

        return mapToProfileResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToProfileResponse(user);
    }

    @Override
    @Transactional
    public ProfileResponse verifyProfile(UUID officerId, ProfileVerificationRequest request) {
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getAccountStatus() != AccountStatus.PENDING_VERIFICATION) {
            throw new RuntimeException("User profile is not pending verification");
        }

        if (request.getStatus() != AccountStatus.VERIFIED && request.getStatus() != AccountStatus.REJECTED) {
            throw new RuntimeException("Invalid status. Must be VERIFIED or REJECTED");
        }

        if (request.getStatus() == AccountStatus.REJECTED && 
            (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty())) {
            throw new RuntimeException("Rejection reason is required when rejecting a profile");
        }

        user.setAccountStatus(request.getStatus());
        user.setVerificationNotes(request.getVerificationNotes());
        user.setVerifiedBy(officer);
        user.setVerifiedAt(LocalDateTime.now());

        if (request.getStatus() == AccountStatus.REJECTED) {
            user.setRejectionReason(request.getRejectionReason());
        } else {
            user.setRejectionReason(null);
        }

        // Flush before notification so the REQUIRES_NEW transaction sees the committed status
        userRepository.saveAndFlush(user);

        // REQ-3 / REQ-4: Notify the user of the verification outcome
        try {
            if (request.getStatus() == AccountStatus.VERIFIED) {
                notificationService.send(
                        user.getId(),
                        NotificationType.ACCOUNT_VERIFIED,
                        "Your account has been verified! You now have full access to RentalPro ET.",
                        user.getId());
            } else if (request.getStatus() == AccountStatus.REJECTED) {
                String msg = String.format(
                        "Your account verification was rejected. Reason: %s. " +
                        "Please update your profile and resubmit.",
                        request.getRejectionReason());
                notificationService.send(
                        user.getId(),
                        NotificationType.ACCOUNT_REJECTED,
                        msg,
                        user.getId());
            }
        } catch (Exception e) {
            log.warn("Failed to send verification outcome notification to user {}: {}",
                    user.getId(), e.getMessage());
        }

        return mapToProfileResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileResponse> getPendingProfiles() {
        List<User> pendingUsers = userRepository.findByAccountStatus(AccountStatus.PENDING_VERIFICATION);
        return pendingUsers.stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    private boolean isProfileComplete(User user) {
        if (user.getDateOfBirth() == null || 
            user.getResidentialAddress() == null || 
            user.getNationalIdNumber() == null || 
            user.getTinNumber() == null || 
            user.getEntityType() == null) {
            return false;
        }

        if (user.getEntityType() == EntityType.BUSINESS) {
            return user.getBusinessRegNumber() != null && !user.getBusinessRegNumber().trim().isEmpty();
        }

        return true;
    }

    private ProfileResponse mapToProfileResponse(User user) {
        ProfileResponse.ProfileResponseBuilder builder = ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .subCityZone(user.getSubCityZone())
                .accountStatus(user.getAccountStatus())
                .dateOfBirth(user.getDateOfBirth())
                .residentialAddress(user.getResidentialAddress())
                .nationalIdNumber(user.getNationalIdNumber())
                .nationalIdDocumentUrl(user.getNationalIdDocumentUrl())
                .tinNumber(user.getTinNumber())
                .entityType(user.getEntityType())
                .businessRegNumber(user.getBusinessRegNumber())
                .businessRegDocumentUrl(user.getBusinessRegDocumentUrl())
                .verificationNotes(user.getVerificationNotes())
                .verifiedAt(user.getVerifiedAt())
                .rejectionReason(user.getRejectionReason())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        if (user.getVerifiedBy() != null) {
            builder.verifiedById(user.getVerifiedBy().getId())
                   .verifiedByName(user.getVerifiedBy().getFirstName() + " " + user.getVerifiedBy().getLastName());
        }

        return builder.build();
    }
}