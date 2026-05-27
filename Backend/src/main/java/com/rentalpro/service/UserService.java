package com.rentalpro.service;

import com.rentalpro.model.dto.request.ProfileUpdateRequest;
import com.rentalpro.model.dto.request.ProfileVerificationRequest;
import com.rentalpro.model.dto.request.RegisterRequest;
import com.rentalpro.model.dto.response.ProfileResponse;
import com.rentalpro.model.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    /**
     * Persists a new user and sends the welcome notification.
     * Returns the saved User entity — token generation is the caller's responsibility.
     */
    User register(RegisterRequest request);

    /**
     * Looks up a user by email. Used by AuthController after authentication succeeds.
     */
    User findByEmail(String email);

    ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request);

    ProfileResponse getProfile(UUID userId);

    ProfileResponse verifyProfile(UUID officerId, ProfileVerificationRequest request);

    List<ProfileResponse> getPendingProfiles();
}