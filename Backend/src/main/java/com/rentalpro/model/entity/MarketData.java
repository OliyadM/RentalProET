package com.rentalpro.model.entity;

import com.rentalpro.model.enums.PropertyType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "market_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String subCity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyType propertyType;

    @Column(nullable = false)
    private Double avgRent;

    private Double minRent;

    private Double maxRent;

    private Integer sampleSize;

    @Column(nullable = false)
    private LocalDate period; // Month/Year of data

    @CreationTimestamp
    private LocalDateTime createdAt;
}