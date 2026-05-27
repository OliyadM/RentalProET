package com.rentalpro.service.impl;

import com.rentalpro.model.dto.response.AnomalyConcentrationPoint;
import com.rentalpro.model.dto.response.RentDensityPoint;
import com.rentalpro.model.entity.Property;
import com.rentalpro.model.entity.RentalContract;
import com.rentalpro.model.entity.RentDeclaration;
import com.rentalpro.model.enums.ContractStatus;
import com.rentalpro.model.enums.PropertyType;
import com.rentalpro.repository.RentalContractRepository;
import com.rentalpro.repository.RentDeclarationRepository;
import com.rentalpro.service.GISAnalyticsService;
import com.rentalpro.util.AddisAbabaNeighborhoods;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GISAnalyticsServiceImpl implements GISAnalyticsService {

    private final RentalContractRepository contractRepository;
    private final RentDeclarationRepository declarationRepository;

    @Override
    public List<RentDensityPoint> getRentDensityHeatmap(
            PropertyType propertyType,
            String subCity,
            LocalDate startDate,
            LocalDate endDate) {
        
        // Get all active contracts
        List<RentalContract> contracts = contractRepository.findByStatus(ContractStatus.ACTIVE);
        
        // Apply filters
        if (propertyType != null) {
            contracts = contracts.stream()
                .filter(c -> c.getRentalUnit().getProperty().getPropertyType() == propertyType)
                .collect(Collectors.toList());
        }
        
        if (subCity != null && !subCity.isEmpty()) {
            contracts = contracts.stream()
                .filter(c -> subCity.equals(c.getRentalUnit().getProperty().getSubCity()))
                .collect(Collectors.toList());
        }
        
        if (startDate != null) {
            contracts = contracts.stream()
                .filter(c -> !c.getStartDate().isBefore(startDate))
                .collect(Collectors.toList());
        }
        
        if (endDate != null) {
            contracts = contracts.stream()
                .filter(c -> !c.getStartDate().isAfter(endDate))
                .collect(Collectors.toList());
        }
        
        // Group by coordinates (exact GPS or centroid)
        Map<String, List<RentalContract>> groupedByLocation = contracts.stream()
            .collect(Collectors.groupingBy(contract -> {
                Property property = contract.getRentalUnit().getProperty();
                AddisAbabaNeighborhoods.Coordinates coords = 
                    AddisAbabaNeighborhoods.getCoordinates(property);
                return coords.getLatitude() + "," + coords.getLongitude();
            }));
        
        // Calculate average rent per location
        return groupedByLocation.entrySet().stream()
            .map(entry -> {
                String[] coords = entry.getKey().split(",");
                List<RentalContract> locationContracts = entry.getValue();
                
                double avgRent = locationContracts.stream()
                    .mapToDouble(RentalContract::getMonthlyRent)
                    .average()
                    .orElse(0.0);
                
                Property firstProperty = locationContracts.get(0).getRentalUnit().getProperty();
                boolean hasExactGPS = AddisAbabaNeighborhoods.hasExactGPS(firstProperty);
                
                return RentDensityPoint.builder()
                    .latitude(Double.parseDouble(coords[0]))
                    .longitude(Double.parseDouble(coords[1]))
                    .subCity(firstProperty.getSubCity())
                    .averageRent(avgRent)
                    .propertyCount((long) locationContracts.size())
                    .hasExactGPS(hasExactGPS)
                    .build();
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<AnomalyConcentrationPoint> getAnomalyConcentration(
            Double minSeverity,
            String subCity,
            LocalDate startDate,
            LocalDate endDate) {
        
        // Get all anomalous declarations
        List<RentDeclaration> declarations = declarationRepository.findByIsAnomaly(true);
        
        // Apply filters
        if (minSeverity != null) {
            declarations = declarations.stream()
                .filter(d -> d.getAnomalyScore() >= minSeverity)
                .collect(Collectors.toList());
        }
        
        if (subCity != null && !subCity.isEmpty()) {
            declarations = declarations.stream()
                .filter(d -> subCity.equals(d.getContract().getRentalUnit().getProperty().getSubCity()))
                .collect(Collectors.toList());
        }
        
        if (startDate != null) {
            declarations = declarations.stream()
                .filter(d -> !d.getDeclarationPeriod().isBefore(startDate))
                .collect(Collectors.toList());
        }
        
        if (endDate != null) {
            declarations = declarations.stream()
                .filter(d -> !d.getDeclarationPeriod().isAfter(endDate))
                .collect(Collectors.toList());
        }
        
        // Group by coordinates
        Map<String, List<RentDeclaration>> groupedByLocation = declarations.stream()
            .collect(Collectors.groupingBy(declaration -> {
                Property property = declaration.getContract().getRentalUnit().getProperty();
                AddisAbabaNeighborhoods.Coordinates coords = 
                    AddisAbabaNeighborhoods.getCoordinates(property);
                return coords.getLatitude() + "," + coords.getLongitude();
            }));
        
        // Calculate anomaly statistics per location
        return groupedByLocation.entrySet().stream()
            .map(entry -> {
                String[] coords = entry.getKey().split(",");
                List<RentDeclaration> locationDeclarations = entry.getValue();
                
                double avgSeverity = locationDeclarations.stream()
                    .mapToDouble(RentDeclaration::getAnomalyScore)
                    .average()
                    .orElse(0.0);
                
                Property firstProperty = locationDeclarations.get(0)
                    .getContract().getRentalUnit().getProperty();
                boolean hasExactGPS = AddisAbabaNeighborhoods.hasExactGPS(firstProperty);
                
                // Get total declarations for this location (not just anomalies)
                long totalDeclarations = declarationRepository.findAll().stream()
                    .filter(d -> {
                        Property p = d.getContract().getRentalUnit().getProperty();
                        AddisAbabaNeighborhoods.Coordinates c = 
                            AddisAbabaNeighborhoods.getCoordinates(p);
                        return c.getLatitude() == Double.parseDouble(coords[0]) &&
                               c.getLongitude() == Double.parseDouble(coords[1]);
                    })
                    .count();
                
                return AnomalyConcentrationPoint.builder()
                    .latitude(Double.parseDouble(coords[0]))
                    .longitude(Double.parseDouble(coords[1]))
                    .subCity(firstProperty.getSubCity())
                    .anomalyCount((long) locationDeclarations.size())
                    .averageSeverity(avgSeverity)
                    .totalDeclarations(totalDeclarations)
                    .hasExactGPS(hasExactGPS)
                    .build();
            })
            .collect(Collectors.toList());
    }
}
