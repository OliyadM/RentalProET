package com.rentalpro.service.impl;

import com.rentalpro.model.dto.response.AIBenchmarkResponse;
import com.rentalpro.model.entity.Property;
import com.rentalpro.model.enums.PropertyType;
import com.rentalpro.repository.MarketDataRepository;
import com.rentalpro.repository.PropertyRepository;
import com.rentalpro.repository.RentDeclarationRepository;
import com.rentalpro.service.AdminService;
import com.rentalpro.service.RentAnalyzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RentAnalyzerServiceImpl implements RentAnalyzerService {

    private final PropertyRepository propertyRepository;
    private final RentDeclarationRepository declarationRepository;
    private final MarketDataRepository marketDataRepository;
    private final AdminService adminService;

    @Override
    public AIBenchmarkResponse calculateFairRent(UUID propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        // Get historical average for the sub-city
        Double historicalAvg = declarationRepository.calculateAverageRentBySubCityAndPeriod(
                property.getSubCity(),
                LocalDate.now().withDayOfMonth(1)
        );

        if (historicalAvg == null) {
            historicalAvg = getDefaultRentForType(property.getPropertyType());
        }

        double baseRent = historicalAvg;

        // Adjust for property type
        baseRent = applyPropertyTypeAdjustment(baseRent, property.getPropertyType());

        // Adjust for building age
        baseRent = applyAgeAdjustment(baseRent, property.getYearBuilt());

        // Calculate confidence based on data availability
        double confidence = calculateConfidence(property.getSubCity());

        // Use the configurable anomaly threshold as the benchmark band width
        double band = adminService.getConfigEntity().getAnomalyThresholdPercentage();
        double minRent = baseRent * (1 - band);
        double maxRent = baseRent * (1 + band);

        return AIBenchmarkResponse.builder()
                .suggestedRent(Math.round(baseRent * 100.0) / 100.0)
                .minRent(Math.round(minRent * 100.0) / 100.0)
                .maxRent(Math.round(maxRent * 100.0) / 100.0)
                .confidenceScore(confidence)
                .marketTrend(determineMarketTrend(property.getSubCity()))
                .reasoning(generateReasoning(property, baseRent, historicalAvg))
                .build();
    }

    private double getDefaultRentForType(PropertyType type) {
        return switch (type) {
            case COMMERCIAL -> 25000.0;
            case MIXED_USE -> 18000.0;
            case INDUSTRIAL -> 15000.0;
            default -> 12000.0; // RESIDENTIAL
        };
    }

    private double applyPropertyTypeAdjustment(double baseRent, PropertyType type) {
        return switch (type) {
            case COMMERCIAL -> baseRent * 1.3;
            case MIXED_USE -> baseRent * 1.1;
            case INDUSTRIAL -> baseRent * 0.9;
            default -> baseRent;
        };
    }

    private double applyAgeAdjustment(double baseRent, Integer yearBuilt) {
        if (yearBuilt == null) return baseRent;

        int age = LocalDate.now().getYear() - yearBuilt;
        if (age < 5) return baseRent * 1.2;
        else if (age < 15) return baseRent;
        else return baseRent * 0.85;
    }

    private double calculateConfidence(String subCity) {
        long propertyCount = propertyRepository.findBySubCity(subCity).size();
        if (propertyCount > 100) return 0.95;
        if (propertyCount > 50) return 0.85;
        if (propertyCount > 20) return 0.75;
        return 0.60;
    }

    private String determineMarketTrend(String subCity) {
        // Simplified - would analyze historical data
        return "STABLE";
    }

    private String generateReasoning(Property property, double suggestedRent, double historicalAvg) {
        return String.format(
                "Based on market data for %s, average rent is ETB %.2f. " +
                        "Your %s property is suggested at ETB %.2f.",
                property.getSubCity(),
                historicalAvg,
                property.getPropertyType(),
                suggestedRent
        );
    }
}