package com.rentalpro.controller;

import com.rentalpro.model.dto.response.AIBenchmarkResponse;
import com.rentalpro.service.RentAnalyzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final RentAnalyzerService analyzerService;

    @GetMapping("/benchmark/{propertyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AIBenchmarkResponse> getBenchmark(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(analyzerService.calculateFairRent(propertyId));
    }
}