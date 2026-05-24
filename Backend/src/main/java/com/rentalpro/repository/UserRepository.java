package com.rentalpro.repository;

import com.rentalpro.model.entity.User;
import com.rentalpro.model.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    
    Optional<User> findByNationalIdNumber(String nationalIdNumber);
    Optional<User> findByTinNumber(String tinNumber);
    List<User> findByAccountStatus(AccountStatus accountStatus);
}