package com.rentalpro.repository;

import com.rentalpro.model.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, UUID> {

    /** Returns the singleton config row if it exists. */
    Optional<SystemConfig> findTopByOrderByUpdatedAtAsc();
}
