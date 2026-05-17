package com.rentalpro.repository;

import com.rentalpro.model.entity.Appeal;
import com.rentalpro.model.enums.AppealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppealRepository extends JpaRepository<Appeal, UUID> {

    List<Appeal> findByContractId(UUID contractId);

    List<Appeal> findByTenantId(UUID tenantId);

    List<Appeal> findByStatus(AppealStatus status);


    Long countByStatus(AppealStatus status);
}