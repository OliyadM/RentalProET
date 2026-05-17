
---

## 📄 Controllers (`controller/`)

Handle HTTP requests, validate input, call services, return responses.

| File | Purpose | Base URL | Key Endpoints |
|------|---------|----------|---------------|
| `AuthController.java` | Authentication | `/auth` | `POST /register`, `POST /login` |
| `PropertyController.java` | Property management | `/properties` | `POST /`, `GET /my-properties`, `GET /{id}` |
| `RentalUnitController.java` | Unit management | `/units` | `POST /property/{id}`, `GET /property/{id}` |
| `RentalContractController.java` | **Core contract workflow** | `/contracts` | `POST /`, `PUT /{id}/submit`, `POST /{id}/confirm`, `POST /{id}/reject`, `GET /by-status/{status}` |
| `RentDeclarationController.java` | Monthly declarations | `/declarations` | `POST /contract/{id}`, `GET /contract/{id}`, `GET /anomalies/{subCity}` |
| `AppealController.java` | Tenant appeals | `/appeals` | `POST /`, `GET /my-appeals`, `GET /pending`, `POST /{id}/resolve` |
| `AnalyticsController.java` | AI benchmarks | `/analytics` | `GET /benchmark/{propertyId}` |

---

## 📄 Services (`service/` & `service/impl/`)

Business logic layer. Interfaces define contracts, implementations contain logic.

### Service Interfaces (`service/`)

| File | Responsibility | Key Methods |
|------|-------------|-------------|
| `UserService.java` | User management | `register()`, `login()`, `getUserById()` |
| `PropertyService.java` | Property CRUD | `createProperty()`, `getPropertiesByOwner()`, `getPropertyById()` |
| `RentalUnitService.java` | Unit management | `createUnit()`, `getUnitsByProperty()`, `getUnitById()` |
| `RentalContractService.java` | **Contract lifecycle** | `createContract()`, `submitContract()`, `confirmContract()`, `rejectContract()`, `terminateContract()`, `getContractsByStatus()` |
| `RentDeclarationService.java` | Monthly declarations | `createDeclaration()`, `getDeclarationsByContract()`, `getAnomalies()`, `calculateTax()` |
| `AppealService.java` | Appeal management | `createAppeal()`, `resolveAppeal()`, `getAppealsByTenant()`, `getPendingAppeals()` |
| `RentAnalyzerService.java` | AI rent analysis | `calculateFairRent()`, `detectAnomaly()`, `getMarketBenchmark()` |
| `AuditLogService.java` | System auditing | `logAction()`, `getAuditLogsByUser()`, `getAuditLogsByEntity()` |

### Service Implementations (`service/impl/`)

| File | Implements | Description |
|------|-----------|-------------|
| `UserServiceImpl.java` | `UserService` | Handles registration, password encoding, JWT generation |
| `PropertyServiceImpl.java` | `PropertyService` | Property CRUD with ownership validation |
| `RentalUnitServiceImpl.java` | `RentalUnitService` | Unit management under properties |
| `RentalContractServiceImpl.java` | `RentalContractService` | **Full contract workflow: DRAFT → PENDING → CONFIRMED → ACTIVE** |
| `RentDeclarationServiceImpl.java` | `RentDeclarationService` | Monthly rent declarations with AI anomaly detection |
| `AppealServiceImpl.java` | `AppealService` | Tenant appeals with staff resolution workflow |
| `RentAnalyzerServiceImpl.java` | `RentAnalyzerService` | AI-powered rent benchmarking using market data |
| `AuditLogServiceImpl.java` | `AuditLogService` | Comprehensive audit trail for all actions |

---

## 📄 DTOs (`model/dto/`)

Data Transfer Objects - define exactly what goes in/out of API.

### Request DTOs (`dto/request/`)

| File | Used In | Fields |
|------|---------|--------|
| `RegisterRequest.java` | `AuthController.register()` | `email`, `password`, `firstName`, `lastName`, `phoneNumber`, `role` |
| `LoginRequest.java` | `AuthController.login()` | `email`, `password` |
| `ContractRequest.java` | `RentalContractController.createContract()` | `unitId`, `tenantId`, `startDate`, `endDate`, `monthlyRent`, `termsAndConditions` |
| `ContractConfirmationRequest.java` | `RentalContractController.confirmContract()` | `signature` (tenant digital signature) |
| `ContractRejectionRequest.java` | `RentalContractController.rejectContract()` | `reason` (why tenant rejected) |
| `AppealRequest.java` | `AppealController.createAppeal()` | `contractId`, `appealType`, `reason`, `evidenceDocuments` |

### Response DTOs (`dto/response/`)

| File | Returns | Purpose |
|------|---------|---------|
| `AuthResponse.java` | Login/register | `token`, `userId`, `email`, `role`, `expiresIn` |
| `PropertyResponse.java` | Property data | `id`, `propertyName`, `address`, `subCity`, `ownerName`, `unitCount` |
| `RentalUnitResponse.java` | Unit data | `id`, `unitNumber`, `floorArea`, `propertyId`, `propertyName` |
| `ContractResponse.java` | **Contract details** | `id`, `unitId`, `tenantId`, `landlordId`, `startDate`, `endDate`, `monthlyRent`, `status`, `tenantSignature`, `landlordSubmittedAt`, `tenantConfirmedAt` |
| `AppealResponse.java` | Appeal details | `id`, `contractId`, `tenantId`, `tenantName`, `appealType`, `reason`, `status`, `resolutionDecision`, `reviewedByName` |
| `RentDeclarationResponse.java` | Declaration data | `id`, `contractId`, `declarationPeriod`, `declaredRent`, `aiBenchmarkRent`, `anomalyScore`, `isAnomaly`, `estimatedTax` |

---

## 📄 Entities (`model/entity/`)

JPA entities = database tables with relationships.

| File | Table | Key Fields | Relationships |
|------|-------|-----------|---------------|
| `User.java` | `users` | `id`, `email`, `password`, `firstName`, `lastName`, `phoneNumber`, `role`, `isActive` | One-to-many: `properties` (as landlord), `landlordContracts`, `tenantContracts` |
| `Property.java` | `properties` | `id`, `propertyName`, `address`, `subCity`, `woreda`, `propertyType`, `latitude`, `longitude`, `isVerified` | Many-to-one: `owner` (User), One-to-many: `units` |
| `RentalUnit.java` | `rental_units` | `id`, `unitNumber`, `floorArea`, `floorLevel`, `numberOfRooms`, `hasParking`, `hasElevator`, `amenities` | Many-to-one: `property`, One-to-many: `contracts` |
| `RentalContract.java` | `rental_contracts` | `id`, `startDate`, `endDate`, `monthlyRent`, `currency`, `status`, `propertyAddress`, `termsAndConditions`, `tenantSignature`, `landlordSignature`, `landlordSubmittedAt`, `tenantConfirmedAt`, `rejectionReason` | Many-to-one: `rentalUnit`, `tenant`, `landlord`; One-to-many: `declarations`, `appeals` |
| `RentDeclaration.java` | `rent_declarations` | `id`, `declarationPeriod`, `declaredRent`, `aiBenchmarkRent`, `anomalyScore`, `isAnomaly`, `anomalyReason`, `taxCompliant`, `estimatedTax`, `isVerified` | Many-to-one: `contract` |
| `Appeal.java` | `appeals` | `id`, `appealType`, `reason`, `evidenceDocuments`, `status`, `resolutionDecision`, `resolutionNotes`, `reviewedAt` | Many-to-one: `contract`, `tenant`, `reviewedBy` |
| `AuditLog.java` | `audit_logs` | `id`, `action`, `entityType`, `entityId`, `details`, `ipAddress`, `timestamp` | Many-to-one: `user` |
| `MarketData.java` | `market_data` | `id`, `subCity`, `propertyType`, `avgRent`, `minRent`, `maxRent`, `sampleSize`, `period` | None (reference data) |

---

## 📄 Enums (`model/enums/`)

| File | Values | Used In |
|------|--------|---------|
| `UserRole.java` | `LANDLORD`, `TENANT`, `SUBCITY_STAFF`, `ADMINISTRATOR` | `User.role` |
| `ContractStatus.java` | `DRAFT`, `PENDING_CONFIRMATION`, `CONFIRMED`, `ACTIVE`, `UNDER_APPEAL`, `UNDER_REVIEW`, `REJECTED`, `TERMINATED`, `EXPIRED` | `RentalContract.status` |
| `AppealStatus.java` | `PENDING`, `RESOLVED` | `Appeal.status` |
| `PropertyType.java` | `APARTMENT`, `VILLA`, `COMMERCIAL`, `MIXED_USE` | `Property.propertyType` |

---

## 📄 Repositories (`repository/`)

Spring Data JPA interfaces - database access.

| File | Entity | Key Methods |
|------|--------|-------------|
| `UserRepository.java` | User | `findByEmail()`, `existsByEmail()`, `findByRole()` |
| `PropertyRepository.java` | Property | `findByOwnerId()`, `findBySubCity()`, `findByIsVerifiedTrue()` |
| `RentalUnitRepository.java` | RentalUnit | `findByPropertyId()`, `findByPropertyOwnerId()` |
| `RentalContractRepository.java` | RentalContract | `findByLandlordId()`, `findByTenantId()`, `findByStatus()`, `existsActiveContractByUnitId()`, `findByIdAndLandlordId()`, `findByIdAndTenantId()` |
| `RentDeclarationRepository.java` | RentDeclaration | `findByContractId()`, `findByIsAnomalyTrue()`, `findByContractRentalUnitPropertySubCity()` |
| `AppealRepository.java` | Appeal | `findByTenantId()`, `findByStatus()`, `findByContractId()`, `existsByContractIdAndStatus()` |
| `AuditLogRepository.java` | AuditLog | `findByUserId()`, `findByEntityTypeAndEntityId()`, `findByTimestampBetween()` |
| `MarketDataRepository.java` | MarketData | `findBySubCityAndPropertyType()`, `findByPeriod()` |

---

## 📄 Security (`security/`)

| File | Purpose | Key Methods |
|------|---------|-------------|
| `JwtTokenProvider.java` | Token generation/validation | `generateToken()`, `validateToken()`, `extractUsername()` |
| `JwtAuthenticationFilter.java` | Request filtering | `doFilterInternal()` - validates token on each request |
| `CustomUserDetailsService.java` | User loading | `loadUserByUsername()` - loads user for Spring Security |

---

## 📄 Configuration (`config/`)

| File | Purpose |
|------|---------|
| `SecurityConfig.java` | Spring Security configuration, CORS, JWT filter chain, role-based access |

---

## 📄 Resources (`src/main/resources/`)

| File | Purpose |
|------|---------|
| `application.properties` | Main configuration (database, JWT, server) |
| `application-local.properties` | Local overrides (gitignored, each dev has own) |
| `db/migration/V1__init.sql` | Flyway migration - initial schema |
| `db/migration/V2__create_properties.sql` | Property table |
| `db/migration/V3__create_units.sql` | Rental unit table |
| `db/migration/V4__create_contracts.sql` | Contract table |
| `db/migration/V5__create_declarations.sql` | Rent declaration table |
| `db/migration/V6__create_appeals.sql` | Appeal table |
| `db/migration/V7__create_audit_logs.sql` | Audit log table |

---

## 📄 Root Files

| File | Purpose |
|------|---------|
| `RentalproApplication.java` | Spring Boot entry point with `@SpringBootApplication` |
| `pom.xml` | Maven dependencies (Spring Boot, PostgreSQL, JWT, etc.) |
| `mvnw` / `mvnw.cmd` | Maven wrapper (run without installing Maven) |
| `.gitignore` | Files to exclude from git (target/, IDE files, secrets) |
| `README.md` | Project overview and quick start |

---

## 🔑 Key Patterns

### 1. Never Return Entities from Controllers
```java
// ❌ BAD - causes lazy loading errors
@GetMapping("/{id}")
public Appeal getAppeal(@PathVariable UUID id) {
    return appealService.findById(id);  // Entity!
}

// ✅ GOOD - use DTO
@GetMapping("/{id}")
public AppealResponse getAppeal(@PathVariable UUID id) {
    return appealService.getAppealById(id);  // DTO!
}