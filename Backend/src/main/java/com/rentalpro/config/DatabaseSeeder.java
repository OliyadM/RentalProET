package com.rentalpro.config;

import com.rentalpro.model.entity.User;
import com.rentalpro.model.enums.AccountStatus;
import com.rentalpro.model.enums.EntityType;
import com.rentalpro.model.enums.UserRole;
import com.rentalpro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs once on every application startup.
 *
 * Guarantees a default ADMINISTRATOR account exists so the Admin Control Panel
 * is accessible on a fresh deployment without any manual bootstrapping.
 *
 * Execution is idempotent: if any ADMINISTRATOR account already exists in the
 * database, this seeder does nothing and logs a skip message.
 *
 * Credentials are read from application.yml (app.seed.admin.*) and can be
 * overridden per-environment via ADMIN_SEED_EMAIL / ADMIN_SEED_PASSWORD /
 * ADMIN_SEED_PHONE environment variables.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.email}")
    private String adminEmail;

    @Value("${app.seed.admin.password}")
    private String adminPassword;

    @Value("${app.seed.admin.phone}")
    private String adminPhone;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdminUser();
    }

    private void seedAdminUser() {
        // Idempotency check — skip if any admin already exists
        if (userRepository.existsByRole(UserRole.ADMINISTRATOR)) {
            log.info("DatabaseSeeder: ADMINISTRATOR account already exists — skipping seed");
            return;
        }

        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .firstName("System")
                .lastName("Admin")
                .phoneNumber(adminPhone)
                .role(UserRole.ADMINISTRATOR)
                .isActive(true)
                // VERIFIED so the account is immediately usable.
                // PENDING_PROFILE or PENDING_VERIFICATION would block access
                // if any security layer checks accountStatus before allowing login.
                .accountStatus(AccountStatus.VERIFIED)
                .entityType(EntityType.INDIVIDUAL)
                // Admins are not scoped to any sub-city
                .subCityZone(null)
                .build();

        userRepository.save(admin);

        // Log email only — never log the password in any form
        log.info("DatabaseSeeder: Default ADMINISTRATOR account created — email: {}", adminEmail);
    }
}
