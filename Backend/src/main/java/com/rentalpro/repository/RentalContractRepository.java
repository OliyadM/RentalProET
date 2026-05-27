package com.rentalpro.repository;

import com.rentalpro.model.entity.RentalContract;
import com.rentalpro.model.enums.ContractStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RentalContractRepository extends JpaRepository<RentalContract, UUID> {

    List<RentalContract> findByLandlordId(UUID landlordId);

    List<RentalContract> findByTenantId(UUID tenantId);

    List<RentalContract> findByRentalUnitId(UUID unitId);

    List<RentalContract> findByStatus(ContractStatus status);

    @Query("SELECT c FROM RentalContract c WHERE c.rentalUnit.id = :unitId AND c.status IN ('PENDING_CONFIRMATION', 'CONFIRMED', 'ACTIVE')")
    List<RentalContract> findActiveOrPendingByUnitId(UUID unitId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM RentalContract c WHERE c.rentalUnit.id = :unitId AND c.status = 'ACTIVE'")
    boolean existsActiveContractByUnitId(UUID unitId);

    Optional<RentalContract> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<RentalContract> findByIdAndLandlordId(UUID id, UUID landlordId);

    @Query("SELECT c FROM RentalContract c " +
           "LEFT JOIN c.landlord l " +
           "LEFT JOIN c.tenant t " +
           "LEFT JOIN c.rentalUnit u " +
           "LEFT JOIN u.property p " +
           "WHERE (:status IS NULL OR c.status = :status) " +
           "AND (:subCity IS NULL OR p.subCity = :subCity) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(l.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.propertyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.address) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<RentalContract> findContractsForOfficer(
        @Param("status") ContractStatus status,
        @Param("subCity") String subCity,
        @Param("search") String search,
        Sort sort
    );
}













//import com.rentalpro.model.entity.RentalContract;
//import com.rentalpro.model.enums.ContractStatus;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.UUID;
//
//@Repository
//public interface RentalContractRepository extends JpaRepository<RentalContract, UUID> {
//
//    List<RentalContract> findByTenantId(UUID tenantId);
//
//    List<RentalContract> findByLandlordId(UUID landlordId);
//
//    List<RentalContract> findByStatus(ContractStatus status);
//
//    List<RentalContract> findByEndDateBeforeAndStatus(LocalDate date, ContractStatus status);
//}