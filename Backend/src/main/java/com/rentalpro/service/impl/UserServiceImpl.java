package com.rentalpro.service.impl;

import com.rentalpro.model.dto.request.ProfileUpdateRequest;
import com.rentalpro.model.dto.request.ProfileVerificationRequest;
import com.rentalpro.model.dto.request.RegisterRequest;
import com.rentalpro.model.dto.response.AuthResponse;
import com.rentalpro.model.dto.response.ProfileResponse;
import com.rentalpro.model.entity.User;
import com.rentalpro.model.enums.AccountStatus;
import com.rentalpro.model.enums.EntityType;
import com.rentalpro.repository.UserRepository;
import com.rentalpro.security.JwtTokenProvider;
import com.rentalpro.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
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

        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .subCityZone(user.getSubCityZone())
                .build();
    }

    @Override
    public AuthResponse login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .subCityZone(user.getSubCityZone())
                .build();
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
        }

        userRepository.save(user);

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

        userRepository.save(user);

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