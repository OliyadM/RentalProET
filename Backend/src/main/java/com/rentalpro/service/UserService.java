package com.rentalpro.service;

import com.rentalpro.model.dto.request.ProfileUpdateRequest;
import com.rentalpro.model.dto.request.ProfileVerificationRequest;
import com.rentalpro.model.dto.request.RegisterRequest;
import com.rentalpro.model.dto.response.AuthResponse;
import com.rentalpro.model.dto.response.ProfileResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(String email, String password);

    ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request);

    ProfileResponse getProfile(UUID userId);

    ProfileResponse verifyProfile(UUID officerId, ProfileVerificationRequest request);

    List<ProfileResponse> getPendingProfiles();
}