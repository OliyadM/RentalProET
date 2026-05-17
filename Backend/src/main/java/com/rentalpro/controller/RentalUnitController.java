package com.rentalpro.controller;

import com.rentalpro.model.dto.response.RentalUnitResponse;
import com.rentalpro.model.entity.RentalUnit;
import com.rentalpro.model.entity.User;
import com.rentalpro.repository.UserRepository;
import com.rentalpro.service.RentalUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/units")
@RequiredArgsConstructor
public class RentalUnitController {

    private final RentalUnitService unitService;
    private final UserRepository userRepository;

    @PostMapping("/property/{propertyId}")
    @PreAuthorize("hasRole('LANDLORD') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<RentalUnitResponse> createUnit(
            @PathVariable UUID propertyId,
            @RequestBody RentalUnit unit) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(unitService.createUnit(propertyId, unit, currentUser.getId()));
    }

    @GetMapping("/property/{propertyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RentalUnitResponse>> getUnitsByProperty(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(unitService.getUnitsByProperty(propertyId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RentalUnitResponse> getUnitById(@PathVariable UUID id) {
        return ResponseEntity.ok(unitService.getUnitById(id));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
