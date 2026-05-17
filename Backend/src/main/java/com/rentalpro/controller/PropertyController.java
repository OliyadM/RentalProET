package com.rentalpro.controller;

import com.rentalpro.model.dto.response.PropertyResponse;
import com.rentalpro.model.entity.Property;
import com.rentalpro.model.entity.User;
import com.rentalpro.repository.UserRepository;
import com.rentalpro.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('LANDLORD') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<Property> createProperty(@RequestBody Property property) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(propertyService.createProperty(currentUser.getId(), property));
    }

    @GetMapping("/my-properties")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<List<PropertyResponse>> getMyProperties() {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(propertyService.getPropertiesByOwner(currentUser.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PropertyResponse> getProperty(@PathVariable UUID id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }

    @GetMapping("/subcity/{subCity}")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<List<PropertyResponse>> getBySubCity(@PathVariable String subCity) {
        return ResponseEntity.ok(propertyService.getPropertiesBySubCity(subCity));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<PropertyResponse> verifyProperty(@PathVariable UUID id) {
        return ResponseEntity.ok(propertyService.verifyProperty(id));
    }
}