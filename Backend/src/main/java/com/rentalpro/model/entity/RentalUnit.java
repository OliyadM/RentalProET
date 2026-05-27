package com.rentalpro.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rental_units")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonIgnoreProperties({"units", "hibernateLazyInitializer", "handler"})
    private Property property;

    @Column(nullable = false)
    private String unitNumber;

    private Double floorArea;

    private Integer floorLevel;

    private Integer numberOfRooms;

    @Builder.Default
    private Boolean hasParking = false;

    @Builder.Default
    private Boolean hasElevator = false;

    private String amenities; // JSON string

    @OneToMany(mappedBy = "rentalUnit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"rentalUnit"})
    private List<RentalContract> contracts;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}