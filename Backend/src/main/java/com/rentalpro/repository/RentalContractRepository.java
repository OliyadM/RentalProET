package com.rentalpro.repository;

import com.rentalpro.model.entity.RentalContract;
import com.rentalpro.model.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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