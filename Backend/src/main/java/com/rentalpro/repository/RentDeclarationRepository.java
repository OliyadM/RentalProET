package com.rentalpro.repository;

import com.rentalpro.model.entity.RentDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RentDeclarationRepository extends JpaRepository<RentDeclaration, UUID> {

    List<RentDeclaration> findByContractId(UUID contractId);

    List<RentDeclaration> findByIsAnomaly(Boolean isAnomaly);

    List<RentDeclaration> findByIsVerified(Boolean isVerified);

    /**
     * Fetch anomalous declarations with the full association chain eagerly loaded
     * to avoid LazyInitializationException and orphaned-proxy errors in GIS queries.
     */
    @Query("""
        SELECT rd FROM RentDeclaration rd
        JOIN FETCH rd.contract c
        JOIN FETCH c.rentalUnit u
        JOIN FETCH u.property p
        WHERE rd.isAnomaly = true
        """)
    List<RentDeclaration> findAnomalousWithProperty();

    /**
     * Fetch all declarations with the full association chain eagerly loaded.
     * Used by GIS total-declaration counts to avoid N+1 lazy loads.
     */
    @Query("""
        SELECT rd FROM RentDeclaration rd
        JOIN FETCH rd.contract c
        JOIN FETCH c.rentalUnit u
        JOIN FETCH u.property p
        """)
    List<RentDeclaration> findAllWithProperty();

    @Query("SELECT rd FROM RentDeclaration rd WHERE rd.contract.rentalUnit.property.subCity = :subCity")
    List<RentDeclaration> findBySubCity(@Param("subCity") String subCity);

    @Query("SELECT AVG(rd.declaredRent) FROM RentDeclaration rd WHERE rd.contract.rentalUnit.property.subCity = :subCity AND rd.declarationPeriod = :period")
    Double calculateAverageRentBySubCityAndPeriod(@Param("subCity") String subCity, @Param("period") LocalDate period);
}