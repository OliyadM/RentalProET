package com.rentalpro.repository;

import com.rentalpro.model.entity.RentalUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RentalUnitRepository extends JpaRepository<RentalUnit, UUID> {
    List<RentalUnit> findByPropertyId(UUID propertyId);
    List<RentalUnit> findByPropertySubCity(String subCity);
}