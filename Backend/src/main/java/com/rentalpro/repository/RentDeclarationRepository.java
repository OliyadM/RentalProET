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

    /**
     * Core price-per-m² query for the benchmark algorithm.
     * Returns individual price-per-m² values (not the average) so the caller
     * can compute both mean and standard deviation in Java.
     * Only includes declarations where floorArea > 0 to avoid division by zero.
     */
    @Query("""
        SELECT rd.declaredRent / u.floorArea
        FROM RentDeclaration rd
        JOIN rd.contract c
        JOIN c.rentalUnit u
        JOIN u.property p
        WHERE u.floorArea IS NOT NULL
          AND u.floorArea > 0
          AND (:subCity IS NULL OR p.subCity = :subCity)
          AND (:siteDesignation IS NULL OR p.siteDesignation = :siteDesignation)
          AND (:propertyType IS NULL OR p.propertyType = :propertyType)
          AND (:ageBracket IS NULL OR
               (:ageBracket = 'NEW'    AND (YEAR(CURRENT_DATE) - p.yearBuilt) < 5)  OR
               (:ageBracket = 'MEDIUM' AND (YEAR(CURRENT_DATE) - p.yearBuilt) BETWEEN 5 AND 20) OR
               (:ageBracket = 'OLD'    AND (YEAR(CURRENT_DATE) - p.yearBuilt) > 20))
          AND (:sizeBandMin IS NULL OR u.floorArea >= :sizeBandMin)
          AND (:sizeBandMax IS NULL OR u.floorArea <= :sizeBandMax)
        """)
    List<Double> findPricePerSqmValues(
            @Param("subCity")          String subCity,
            @Param("siteDesignation")  String siteDesignation,
            @Param("propertyType")     com.rentalpro.model.enums.PropertyType propertyType,
            @Param("ageBracket")       String ageBracket,
            @Param("sizeBandMin")      Double sizeBandMin,
            @Param("sizeBandMax")      Double sizeBandMax);
}