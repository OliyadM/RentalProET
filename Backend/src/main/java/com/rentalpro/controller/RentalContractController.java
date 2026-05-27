package com.rentalpro.controller;

import com.rentalpro.model.dto.request.ContractRequest;
import com.rentalpro.model.dto.request.ContractConfirmationRequest;
import com.rentalpro.model.dto.request.ContractRejectionRequest;
import com.rentalpro.model.dto.response.ContractResponse;
import com.rentalpro.model.entity.User;
import com.rentalpro.model.enums.ContractStatus;
import com.rentalpro.repository.UserRepository;
import com.rentalpro.service.RentalContractService;
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
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class RentalContractController {

    private final RentalContractService contractService;
    private final UserRepository userRepository;

    // ==================== LANDLORD ENDPOINTS ====================

    @PostMapping
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<ContractResponse> createContract(@Valid @RequestBody ContractRequest request) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(contractService.createContract(request, currentUser.getId()));
    }

    @PutMapping("/{contractId}/submit")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<ContractResponse> submitContract(@PathVariable UUID contractId) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(contractService.submitContract(contractId, currentUser.getId()));
    }

    @PutMapping("/{contractId}")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<ContractResponse> updateContract(
            @PathVariable UUID contractId,
            @Valid @RequestBody ContractRequest request) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(contractService.updateContractDraft(contractId, request, currentUser.getId()));
    }

    @GetMapping("/my-contracts/landlord")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<List<ContractResponse>> getMyContractsAsLandlord() {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(contractService.getMyContractsAsLandlord(currentUser.getId()));
    }

    // ==================== TENANT ENDPOINTS ====================

    @PostMapping("/{contractId}/confirm")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ContractResponse> confirmContract(
            @PathVariable UUID contractId,
            @Valid @RequestBody ContractConfirmationRequest request) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(contractService.confirmContract(contractId, currentUser.getId(), request.getSignature()));
    }

    @PostMapping("/{contractId}/reject")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ContractResponse> rejectContract(
            @PathVariable UUID contractId,
            @Valid @RequestBody ContractRejectionRequest request) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(contractService.rejectContract(contractId, currentUser.getId(), request.getReason()));
    }

    @GetMapping("/my-contracts/tenant")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<List<ContractResponse>> getMyContractsAsTenant() {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(contractService.getMyContractsAsTenant(currentUser.getId()));
    }

    // ==================== SHARED ENDPOINTS ====================

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ContractResponse> getContract(@PathVariable UUID id) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(contractService.getContractByIdForUser(id, currentUser.getId()));
    }

    // ==================== STAFF/ADMIN ENDPOINTS ====================

    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<List<ContractResponse>> getContractsByStatus(@PathVariable ContractStatus status) {
        return ResponseEntity.ok(contractService.getContractsByStatus(status));
    }

    @GetMapping("/by-unit/{unitId}")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR', 'LANDLORD')")
    public ResponseEntity<List<ContractResponse>> getContractsByUnit(@PathVariable UUID unitId) {
        return ResponseEntity.ok(contractService.getContractsByUnit(unitId));
    }

    @PostMapping("/{contractId}/terminate")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<ContractResponse> terminateContract(
            @PathVariable UUID contractId,
            @RequestParam String reason) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(contractService.terminateContract(contractId, reason, currentUser.getId()));
    }

    // ==================== OFFICER ENDPOINTS ====================

    @GetMapping("/pending-review")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<List<ContractResponse>> getPendingOfficerReview() {
        return ResponseEntity.ok(contractService.getPendingOfficerReview());
    }

    @GetMapping("/officer/all")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<List<ContractResponse>> getContractsForOfficer(
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(required = false) String subCity,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "newest,desc") String sort) {
        return ResponseEntity.ok(contractService.getContractsForOfficer(status, subCity, search, sort));
    }

    @PostMapping("/{contractId}/approve")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<ContractResponse> approveContract(@PathVariable UUID contractId) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(contractService.approveContract(contractId, currentUser.getId()));
    }

    @PostMapping("/{contractId}/reject-by-officer")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<ContractResponse> rejectContractByOfficer(
            @PathVariable UUID contractId,
            @RequestParam String reason) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(contractService.rejectContractByOfficer(contractId, currentUser.getId(), reason));
    }

    // ==================== UTILITY ====================

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}