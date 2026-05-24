# DatabaseSeeder — Design

## Files to Create or Modify

### New Files

```
Backend/src/main/java/com/rentalpro/
└── config/
    └── DatabaseSeeder.java          ← new

Backend/src/main/resources/
└── application.yml                  ← modified (add app.seed.* block)
```

---

## 1. `application.yml` — Add Seed Configuration Block

Add the following block to `application.yml`. The values shown are the development defaults. Production deployments override them via environment variables.

```yaml
app:
  seed:
    admin:
      email: ${ADMIN_SEED_EMAIL:admin@rentalpro.et}
      password: ${ADMIN_SEED_PASSWORD:Admin@123}
      phone: ${ADMIN_SEED_PHONE:+251900000000}
```

---

## 2. `DatabaseSeeder.java` — Full Class Design

**Package:** `com.rentalpro.config`

**Implements:** `ApplicationRunner` (runs after the full Spring context and all beans are initialized, including JPA — safer than `CommandLineRunner` for database operations)

**Dependencies injected:**
- `UserRepository` — to check existence and save
- `PasswordEncoder` — to BCrypt-encode the password before storage

**Configuration properties bound via `@Value`:**
- `${app.seed.admin.email}`
- `${app.seed.admin.password}`
- `${app.seed.admin.phone}`

### Execution Flow

```
ApplicationRunner.run() called on startup
    │
    ├─ Query: userRepository.existsByRole(UserRole.ADMINISTRATOR)
    │
    ├─ EXISTS → log "Admin account already exists, skipping seed" → return
    │
    └─ NOT EXISTS →
            Build User object with all required fields
            Encode password with BCryptPasswordEncoder
            Save to database with userRepository.save()
            Log "Default admin account created: {email}"
```

### Entity Field Mapping

| User Field | Value | Reason |
|-----------|-------|--------|
| `id` | auto-generated UUID | JPA `@GeneratedValue` |
| `email` | from `app.seed.admin.email` | configurable |
| `password` | BCrypt(`app.seed.admin.password`) | never plain text |
| `firstName` | `"System"` | fixed display name |
| `lastName` | `"Admin"` | fixed display name |
| `phoneNumber` | from `app.seed.admin.phone` | unique constraint requires a value |
| `role` | `UserRole.ADMINISTRATOR` | required for admin access |
| `isActive` | `true` | account must be usable immediately |
| `accountStatus` | `AccountStatus.ACTIVE` | `PENDING_PROFILE` would block login via `isEnabled()` |
| `entityType` | `EntityType.INDIVIDUAL` | non-nullable enum, admin is not a business |
| `subCityZone` | `null` | admins are not scoped to any sub-city |
| `dateOfBirth` | `null` | optional KYC field, not required for admin |
| `nationalIdNumber` | `null` | optional, unique constraint allows null |
| `tinNumber` | `null` | optional, unique constraint allows null |
| `verifiedBy` | `null` | admin is self-authorizing |
| `verifiedAt` | `null` | not applicable |

### Repository Addition Required

`UserRepository` currently has no method to check existence by role. Add:

```java
boolean existsByRole(UserRole role);
```

This is a Spring Data derived query — no `@Query` annotation needed.

---

## 3. Complete `DatabaseSeeder.java` Source

```java
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
                .accountStatus(AccountStatus.ACTIVE)
                .entityType(EntityType.INDIVIDUAL)
                .subCityZone(null)
                .build();

        userRepository.save(admin);
        log.info("DatabaseSeeder: Default ADMINISTRATOR account created — email: {}", adminEmail);
    }
}
```

---

## 4. `UserRepository` Addition

```java
// Add to UserRepository.java
boolean existsByRole(UserRole role);
```

---

## Key Design Decisions

**Why `ApplicationRunner` over `CommandLineRunner`?**
Both run after context initialization, but `ApplicationRunner` receives typed `ApplicationArguments` which is more idiomatic for Spring Boot. More importantly, both run after all `@Bean` methods and JPA schema updates (`ddl-auto: update`) have completed, making it safe to query the database immediately.

**Why `existsByRole()` instead of `existsByEmail()`?**
Checking by email would require the seeder to know the configured email at compile time, creating a circular dependency between the check and the config value. Checking by role is config-independent — if any admin exists (even one created manually), seeding is skipped.

**Why `AccountStatus.ACTIVE` instead of `PENDING_PROFILE`?**
The `User.isEnabled()` method returns `isActive`. However, if other parts of the codebase check `accountStatus` before allowing login or API access, `PENDING_PROFILE` would silently block the admin. Setting it to `ACTIVE` ensures the account works immediately on a fresh deployment without requiring a separate verification step.

**Why not `@Profile("!prod")`?**
A production server with a fresh database (e.g. after a disaster recovery restore) needs the admin account just as much as a dev machine. Excluding prod would mean every production deployment requires a manual bootstrap step, which is error-prone and undocumented.

**Why `saveAndFlush()` is not used here?**
`@Transactional` on `run()` means the INSERT will be committed when the method returns. `save()` is sufficient — there is no subsequent read within the same transaction that would require the flush.
