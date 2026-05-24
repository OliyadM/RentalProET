package com.rentalpro.controller;

import com.rentalpro.model.dto.request.ProfileUpdateRequest;
import com.rentalpro.model.dto.request.ProfileVerificationRequest;
import com.rentalpro.model.dto.response.ProfileResponse;
import com.rentalpro.model.entity.User;
import com.rentalpro.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @PostMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateProfile(user.getId(), request));
    }

    @GetMapping("/profile/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileResponse> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getProfile(user.getId()));
    }

    @GetMapping("/profile/{userId}")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @GetMapping("/profiles/pending")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<List<ProfileResponse>> getPendingProfiles() {
        return ResponseEntity.ok(userService.getPendingProfiles());
    }

    @PostMapping("/profiles/verify")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<ProfileResponse> verifyProfile(
            @AuthenticationPrincipal User officer,
            @Valid @RequestBody ProfileVerificationRequest request) {
        return ResponseEntity.ok(userService.verifyProfile(officer.getId(), request));
    }
}
