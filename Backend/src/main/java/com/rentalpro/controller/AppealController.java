package com.rentalpro.controller;

import com.rentalpro.model.dto.request.AppealRequest;
import com.rentalpro.model.dto.response.AppealResponse;
import com.rentalpro.model.entity.User;
import com.rentalpro.repository.UserRepository;
import com.rentalpro.service.AppealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appeals")
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<AppealResponse> createAppeal(@Valid @RequestBody AppealRequest request) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(appealService.createAppeal(request, currentUser.getId()));
    }

    @PostMapping("/{appealId}/resolve")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<AppealResponse> resolveAppeal(
            @PathVariable UUID appealId,
            @RequestParam String decision,
            @RequestParam(required = false) String notes) {
        User currentUser = getCurrentUser();
        String finalNotes = (notes == null) ? "Resolved" : notes;
        return ResponseEntity.ok(appealService.resolveAppeal(appealId, decision, finalNotes, currentUser.getId()));
    }

    // NEW: Reject endpoint to fix the 404/500 NoResourceFound error
    @PostMapping("/{appealId}/reject")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<AppealResponse> rejectAppeal(
            @PathVariable UUID appealId,
            @RequestParam(required = false) String reason) {
        User currentUser = getCurrentUser();
        String finalReason = (reason == null) ? "Insufficient evidence." : reason;
        return ResponseEntity.ok(appealService.rejectAppeal(appealId, finalReason, currentUser.getId()));
    }

    @GetMapping("/my-appeals")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<List<AppealResponse>> getMyAppeals() {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(appealService.getAppealsByTenant(currentUser.getId()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<List<AppealResponse>> getPendingAppeals() {
        return ResponseEntity.ok(appealService.getPendingAppeals());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppealResponse> getAppeal(@PathVariable UUID id) {
        return ResponseEntity.ok(appealService.getAppealById(id));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}