# DatabaseSeeder — Requirements

## Context

A full scan of the RentalPro ET backend confirms there is **no existing admin account initialization** of any kind:

- No `CommandLineRunner` or `ApplicationRunner` implementations exist
- No `@PostConstruct` seeding methods exist
- No `data.sql`, `schema.sql`, or `import.sql` files exist in `src/main/resources`
- No Flyway or Liquibase migration scripts exist
- The `ADMINISTRATOR` role exists only in the `UserRole` enum — no user row has ever been assigned to it
- The only way to create users currently is through `POST /api/auth/register`, which does not expose the `ADMINISTRATOR` role in the frontend form

This means every fresh database deployment has no admin account, making the Admin Control Panel completely inaccessible until one is manually created via a raw API call.

---

## Problem Statement

The system requires a guaranteed, secure default administrator account to exist on every application startup. Without it:

1. The Admin Control Panel (`/admin/dashboard`) is unreachable on a fresh deployment
2. System configuration (tax rate, anomaly threshold, rent cap) cannot be set before officers start using the platform
3. No officers can be provisioned through the UI, blocking the entire onboarding flow
4. Developers and testers must manually fire raw HTTP requests to bootstrap the system

---

## Requirements

### REQ-1 — Idempotent Execution
The seeder MUST check whether an admin account already exists before creating one. If an account with `role = ADMINISTRATOR` already exists in the database, the seeder MUST do nothing. Running the application multiple times must never create duplicate admin accounts or throw errors.

### REQ-2 — Correct User Entity Population
The seeder MUST populate all non-nullable fields on the `User` entity to avoid constraint violations:
- `email` — configurable, with a safe default
- `password` — BCrypt-encoded, never stored in plain text
- `firstName`, `lastName` — human-readable identity
- `phoneNumber` — unique constraint, must be populated
- `role` — must be `UserRole.ADMINISTRATOR`
- `isActive` — must be `true`
- `accountStatus` — must be `AccountStatus.ACTIVE` (not `PENDING_PROFILE`, which would block login)
- `entityType` — must be `EntityType.INDIVIDUAL`
- `subCityZone` — must be `null` (admins are not scoped to a sub-city)

### REQ-3 — Externally Configurable Credentials
The default admin email and password MUST be configurable via `application.yml` / environment variables. They must NOT be hardcoded as string literals in Java source code. This allows different credentials per environment (dev, staging, prod) without code changes.

### REQ-4 — Environment Safety
The seeder MUST run in all Spring profiles including production. A production deployment with an empty database is just as likely to need seeding as a development one. Profile-gating (`@Profile("!prod")`) is explicitly NOT acceptable.

### REQ-5 — Startup Logging
The seeder MUST log a clear message when it creates the admin account, and a different message when it skips creation because the account already exists. This makes deployment logs auditable.

### REQ-6 — No Plain-Text Credentials in Logs
The seeder MUST NOT log the admin password in any form — not plain text, not encoded. Only the email address may appear in logs.

### REQ-7 — Failure Isolation
If the seeder fails for any reason (e.g. database not yet ready, constraint violation), it MUST throw a descriptive exception that halts application startup. A silent failure that leaves the system in an inconsistent state is not acceptable.

---

## Credentials Specification (Defaults)

| Field | Default Value | Source |
|-------|--------------|--------|
| Email | `admin@rentalpro.et` | `application.yml` → `app.seed.admin.email` |
| Password | `Admin@123` | `application.yml` → `app.seed.admin.password` |
| First Name | `System` | Hardcoded (not sensitive) |
| Last Name | `Admin` | Hardcoded (not sensitive) |
| Phone | `+251900000000` | `application.yml` → `app.seed.admin.phone` |

The production deployment MUST override `app.seed.admin.email` and `app.seed.admin.password` via environment variables before going live.
