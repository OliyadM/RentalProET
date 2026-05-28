package com.rentalpro.service.impl;

import com.rentalpro.model.dto.response.AIBenchmarkResponse;
import com.rentalpro.model.entity.Property;
import com.rentalpro.model.entity.RentalUnit;
import com.rentalpro.model.enums.PropertyType;
import com.rentalpro.repository.PropertyRepository;
import com.rentalpro.repository.RentDeclarationRepository;
import com.rentalpro.repository.RentalUnitRepository;
import com.rentalpro.service.AdminService;
import com.rentalpro.service.RentAnalyzerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentAnalyzerServiceImpl implements RentAnalyzerService {

    private final PropertyRepository propertyRepository;
    private final RentalUnitRepository unitRepository;
    private final RentDeclarationRepository declarationRepository;
    private final AdminService adminService;

    // ── Minimum records needed to trust a comparison group ───────────────────
    private static final int MIN_SAMPLE = 3;

    // ── Size bands in m² ─────────────────────────────────────────────────────
    private static final double SMALL_MAX  =  50.0;
    private static final double MEDIUM_MAX = 100.0;
    private static final double LARGE_MAX  = 200.0;

    // ── Hardcoded price-per-m² defaults (ETB/m²) when no data at all ─────────
    private static final double DEFAULT_PPM2_COMMERCIAL  = 350.0;
    private static final double DEFAULT_PPM2_MIXED       = 250.0;
    private static final double DEFAULT_PPM2_WAREHOUSE   = 150.0;
    private static final double DEFAULT_PPM2_APARTMENT   = 200.0;
    private static final double DEFAULT_PPM2_HOUSE       = 160.0;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public AIBenchmarkResponse calculateFairRent(UUID propertyId) {
        // Legacy path — no unit context, use property-level estimate
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        return buildPropertyLevelBenchmark(property);
    }

    @Override
    public AIBenchmarkResponse calculateFairRent(UUID propertyId, UUID unitId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        RentalUnit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unit not found"));

        Double floorArea = unit.getFloorArea();
        if (floorArea == null || floorArea <= 0) {
            // No area data — fall back to property-level estimate
            log.debug("Unit {} has no floor area — using property-level benchmark", unitId);
            return buildPropertyLevelBenchmark(property);
        }

        return buildPricePerSqmBenchmark(property, unit, floorArea);
    }

    // ── Price-per-m² benchmark (main algorithm) ───────────────────────────────

    private AIBenchmarkResponse buildPricePerSqmBenchmark(
            Property property, RentalUnit unit, double floorArea) {

        String subCity         = property.getSubCity();
        String siteDesignation = property.getSiteDesignation();
        PropertyType propType  = property.getPropertyType();
        String ageBracket      = getAgeBracket(property.getYearBuilt());
        double[] sizeBand      = getSizeBand(floorArea);

        // ── 6-level fallback chain ────────────────────────────────────────────
        BenchmarkResult result = null;
        int fallbackLevel = 0;

        // Level 1: subCity + propType + siteDesig + ageBracket + sizeBand
        result = query(subCity, siteDesignation, propType, ageBracket, sizeBand[0], sizeBand[1]);
        if (result != null) { fallbackLevel = 1; }

        // Level 2: subCity + propType + siteDesig + ageBracket
        if (result == null) {
            result = query(subCity, siteDesignation, propType, ageBracket, null, null);
            if (result != null) fallbackLevel = 2;
        }

        // Level 3: subCity + propType + siteDesig
        if (result == null) {
            result = query(subCity, siteDesignation, propType, null, null, null);
            if (result != null) fallbackLevel = 3;
        }

        // Level 4: subCity + siteDesig
        if (result == null) {
            result = query(subCity, siteDesignation, null, null, null, null);
            if (result != null) fallbackLevel = 4;
        }

        // Level 5: citywide + propType
        if (result == null) {
            result = query(null, null, propType, null, null, null);
            if (result != null) fallbackLevel = 5;
        }

        // Level 6: hardcoded defaults
        if (result == null) {
            fallbackLevel = 6;
            double defaultPpm2 = getDefaultPricePerSqm(propType);
            result = new BenchmarkResult(defaultPpm2, 0.0, 0);
            log.debug("Using hardcoded default ETB/m² for {} in {}", propType, subCity);
        }

        double avgPpm2    = result.avg;
        double stdDev     = result.stdDev;
        int    sampleSize = result.count;

        double suggestedRent = round(avgPpm2 * floorArea);

        // Use stdDev-based range when we have enough data, else ±25% fallback
        double lowerBound, upperBound;
        if (sampleSize >= 5 && stdDev > 0) {
            lowerBound = round((avgPpm2 - stdDev) * floorArea);
            upperBound = round((avgPpm2 + stdDev) * floorArea);
        } else {
            lowerBound = round(suggestedRent * 0.75);
            upperBound = round(suggestedRent * 1.25);
        }

        double confidence = calculateConfidence(sampleSize, fallbackLevel);

        String reasoning = buildReasoning(
                property, unit, floorArea, avgPpm2, suggestedRent,
                sampleSize, fallbackLevel, ageBracket);

        return AIBenchmarkResponse.builder()
                .suggestedRent(suggestedRent)
                .minRent(Math.max(0, lowerBound))
                .maxRent(upperBound)
                .confidenceScore(confidence)
                .marketTrend("STABLE")
                .reasoning(reasoning)
                .pricePerSqm(round(avgPpm2))
                .stdDev(round(stdDev))
                .sampleSize(sampleSize)
                .fallbackLevel(fallbackLevel)
                .unitFloorArea(floorArea)
                .build();
    }

    // ── Property-level benchmark (legacy / no floor area) ────────────────────

    private AIBenchmarkResponse buildPropertyLevelBenchmark(Property property) {
        Double historicalAvg = declarationRepository.calculateAverageRentBySubCityAndPeriod(
                property.getSubCity(), LocalDate.now().withDayOfMonth(1));

        if (historicalAvg == null) {
            historicalAvg = getDefaultRentForType(property.getPropertyType());
        }

        double baseRent = applyPropertyTypeAdjustment(historicalAvg, property.getPropertyType());
        baseRent = applyAgeAdjustment(baseRent, property.getYearBuilt());

        double band = adminService.getConfigEntity().getAnomalyThresholdPercentage();

        return AIBenchmarkResponse.builder()
                .suggestedRent(round(baseRent))
                .minRent(round(baseRent * (1 - band)))
                .maxRent(round(baseRent * (1 + band)))
                .confidenceScore(0.60)
                .marketTrend("STABLE")
                .reasoning(String.format(
                        "Based on sub-city average for %s (%s). No unit floor area available for m² normalization.",
                        property.getSubCity(), property.getPropertyType()))
                .pricePerSqm(null)
                .stdDev(null)
                .sampleSize(null)
                .fallbackLevel(6)
                .unitFloorArea(null)
                .build();
    }

    // ── Query helper ──────────────────────────────────────────────────────────

    private BenchmarkResult query(
            String subCity, String siteDesignation, PropertyType propType,
            String ageBracket, Double sizeBandMin, Double sizeBandMax) {

        List<Double> values = declarationRepository.findPricePerSqmValues(
                subCity, siteDesignation, propType, ageBracket, sizeBandMin, sizeBandMax);

        if (values == null || values.size() < MIN_SAMPLE) return null;

        double avg    = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double stdDev = computeStdDev(values, avg);
        return new BenchmarkResult(avg, stdDev, values.size());
    }

    private record BenchmarkResult(double avg, double stdDev, int count) {}

    // ── Statistical helpers ───────────────────────────────────────────────────

    private double computeStdDev(List<Double> values, double mean) {
        if (values.size() < 2) return 0.0;
        double variance = values.stream()
                .mapToDouble(v -> (v - mean) * (v - mean))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    // ── Classification helpers ────────────────────────────────────────────────

    private String getAgeBracket(Integer yearBuilt) {
        if (yearBuilt == null) return null;
        int age = LocalDate.now().getYear() - yearBuilt;
        if (age < 5)  return "NEW";
        if (age <= 20) return "MEDIUM";
        return "OLD";
    }

    /** Returns [min, max] for the size band, or [null, null] if no band applies. */
    private double[] getSizeBand(double floorArea) {
        if (floorArea <= SMALL_MAX)  return new double[]{0,          SMALL_MAX};
        if (floorArea <= MEDIUM_MAX) return new double[]{SMALL_MAX,  MEDIUM_MAX};
        if (floorArea <= LARGE_MAX)  return new double[]{MEDIUM_MAX, LARGE_MAX};
        return new double[]{LARGE_MAX, Double.MAX_VALUE};
    }

    private double calculateConfidence(int sampleSize, int fallbackLevel) {
        // Base confidence from sample size
        double base;
        if (sampleSize >= 20) base = 0.95;
        else if (sampleSize >= 10) base = 0.85;
        else if (sampleSize >= 5)  base = 0.75;
        else if (sampleSize >= 3)  base = 0.65;
        else base = 0.50;

        // Penalise for each fallback level beyond 1
        double penalty = (fallbackLevel - 1) * 0.05;
        return Math.max(0.30, base - penalty);
    }

    // ── Default values ────────────────────────────────────────────────────────

    private double getDefaultPricePerSqm(PropertyType type) {
        return switch (type) {
            case COMMERCIAL_BUILDING  -> DEFAULT_PPM2_COMMERCIAL;
            case MIXED_USE_BUILDING   -> DEFAULT_PPM2_MIXED;
            case WAREHOUSE_INDUSTRIAL -> DEFAULT_PPM2_WAREHOUSE;
            case APARTMENT_BUILDING   -> DEFAULT_PPM2_APARTMENT;
            default                   -> DEFAULT_PPM2_HOUSE;
        };
    }

    private double getDefaultRentForType(PropertyType type) {
        return switch (type) {
            case COMMERCIAL_BUILDING  -> 25000.0;
            case MIXED_USE_BUILDING   -> 18000.0;
            case WAREHOUSE_INDUSTRIAL -> 15000.0;
            case APARTMENT_BUILDING   -> 14000.0;
            default                   -> 12000.0;
        };
    }

    private double applyPropertyTypeAdjustment(double base, PropertyType type) {
        return switch (type) {
            case COMMERCIAL_BUILDING  -> base * 1.3;
            case MIXED_USE_BUILDING   -> base * 1.1;
            case WAREHOUSE_INDUSTRIAL -> base * 0.9;
            case APARTMENT_BUILDING   -> base * 1.05;
            default                   -> base;
        };
    }

    private double applyAgeAdjustment(double base, Integer yearBuilt) {
        if (yearBuilt == null) return base;
        int age = LocalDate.now().getYear() - yearBuilt;
        if (age < 5)  return base * 1.2;
        if (age < 15) return base;
        return base * 0.85;
    }

    // ── Reasoning text ────────────────────────────────────────────────────────

    private String buildReasoning(
            Property property, RentalUnit unit, double floorArea,
            double avgPpm2, double suggestedRent,
            int sampleSize, int fallbackLevel, String ageBracket) {

        String matchDesc = switch (fallbackLevel) {
            case 1 -> String.format("%d comparable %s units in %s (%s age, similar size)",
                    sampleSize, property.getPropertyType(), property.getSubCity(), ageBracket);
            case 2 -> String.format("%d comparable %s units in %s (%s age)",
                    sampleSize, property.getPropertyType(), property.getSubCity(), ageBracket);
            case 3 -> String.format("%d comparable %s units in %s",
                    sampleSize, property.getPropertyType(), property.getSubCity());
            case 4 -> String.format("%d properties in %s (all types)",
                    sampleSize, property.getSubCity());
            case 5 -> String.format("%d city-wide %s properties",
                    sampleSize, property.getPropertyType());
            default -> "hardcoded defaults (no comparable data yet)";
        };

        return String.format(
                "Unit %s (%.0fm²) in %s. Market rate: ETB %.0f/m² based on %s. " +
                "Suggested rent: ETB %.0f. Level %d match.",
                unit.getUnitNumber(), floorArea, property.getSubCity(),
                avgPpm2, matchDesc, suggestedRent, fallbackLevel);
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
