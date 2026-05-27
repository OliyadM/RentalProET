package com.rentalpro.controller;

import com.rentalpro.model.dto.response.AIBenchmarkResponse;
import com.rentalpro.model.dto.response.AnomalyConcentrationPoint;
import com.rentalpro.model.dto.response.RentDensityPoint;
import com.rentalpro.model.enums.PropertyType;
import com.rentalpro.service.GISAnalyticsService;
import com.rentalpro.service.RentAnalyzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final RentAnalyzerService analyzerService;
    private final GISAnalyticsService gisAnalyticsService;

    @GetMapping("/benchmark/{propertyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AIBenchmarkResponse> getBenchmark(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(analyzerService.calculateFairRent(propertyId));
    }

    // ==================== GIS HEATMAP ENDPOINTS ====================

    @GetMapping("/heatmap/rent-density")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<List<RentDensityPoint>> getRentDensityHeatmap(
            @RequestParam(required = false) PropertyType propertyType,
            @RequestParam(required = false) String subCity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(gisAnalyticsService.getRentDensityHeatmap(propertyType, subCity, startDate, endDate));
    }

    @GetMapping("/heatmap/anomaly-concentration")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<List<AnomalyConcentrationPoint>> getAnomalyConcentration(
            @RequestParam(required = false) Double minSeverity,
            @RequestParam(required = false) String subCity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(gisAnalyticsService.getAnomalyConcentration(minSeverity, subCity, startDate, endDate));
    }
}