package com.rentalpro.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rentalpro.model.enums.PropertyStatus;
import com.rentalpro.model.enums.PropertyType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String propertyName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String subCity;

    @Column(nullable = false)
    private String woreda;

    @Column(nullable = false)
    private String kebele;

    @Column(nullable = false)
    private String siteDesignation; // Residential / Commercial / Mixed

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyType propertyType;

    private String titleDeedUrl;

    // Simple Double fields instead of PostGIS geometry
    private Double latitude;
    private Double longitude;

    private Double totalArea;

    private Integer yearBuilt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus status = PropertyStatus.PENDING_OFFICER_REVIEW;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    // Add this field inside the class
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"property"})
    private List<RentalUnit> units;
}