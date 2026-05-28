package com.rentalpro.config;

import com.rentalpro.model.entity.*;
import com.rentalpro.model.enums.*;
import com.rentalpro.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Runs once on every application startup.
 *
 * Phase 1 — Admin bootstrap (always runs, idempotent by role check).
 * Phase 2 — Demo dataset (only runs when the DB has no users at all).
 *
 * The demo dataset is designed to showcase:
 *   • The 6-level price-per-m² benchmark engine (Bole cluster gives a clear
 *     statistical average; anomaly declarations trigger HIGH/MEDIUM alerts).
 *   • The GIS Heatmap (every property has realistic Addis Ababa coordinates).
 *   • The full contract lifecycle (DRAFT → ACTIVE).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements ApplicationRunner {

    // ── Repositories ──────────────────────────────────────────────────────────
    private final UserRepository             userRepository;
    private final PropertyRepository         propertyRepository;
    private final RentalUnitRepository       unitRepository;
    private final RentalContractRepository   contractRepository;
    private final RentDeclarationRepository  declarationRepository;
    private final PasswordEncoder            passwordEncoder;

    // ── Admin credentials from application.yml ────────────────────────────────
    @Value("${app.seed.admin.email}")    private String adminEmail;
    @Value("${app.seed.admin.password}") private String adminPassword;
    @Value("${app.seed.admin.phone}")    private String adminPhone;

    // ── Shared demo password ──────────────────────────────────────────────────
    private static final String DEMO_PASSWORD = "Demo@1234";

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdminUser();
        seedDemoData();
    }

    // =========================================================================
    // Phase 1 — Admin bootstrap
    // =========================================================================

    private void seedAdminUser() {
        if (userRepository.existsByRole(UserRole.ADMINISTRATOR)) {
            log.info("DatabaseSeeder: ADMINISTRATOR already exists — skipping admin seed");
            return;
        }
        userRepository.save(User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .firstName("System").lastName("Admin")
                .phoneNumber(adminPhone)
                .role(UserRole.ADMINISTRATOR)
                .isActive(true)
                .accountStatus(AccountStatus.VERIFIED)
                .entityType(EntityType.INDIVIDUAL)
                .subCityZone(null)
                .build());
        log.info("DatabaseSeeder: ADMINISTRATOR created — {}", adminEmail);
    }

    // =========================================================================
    // Phase 2 — Demo dataset
    // =========================================================================

    private void seedDemoData() {
        // Only seed when the DB is completely empty (no users at all)
        if (userRepository.count() > 0) {
            log.info("DatabaseSeeder: Users already exist — skipping demo data seed");
            return;
        }

        log.info("DatabaseSeeder: Seeding demo dataset for defence...");

        // ── Officers ──────────────────────────────────────────────────────────
        User officerBole   = officer("Abebe",  "Girma",   "officer.bole@rentalpro.et",   "0911000001", "Bole");
        User officerKirkos = officer("Tigist", "Haile",   "officer.kirkos@rentalpro.et", "0911000002", "Kirkos");

        // ── Landlords ─────────────────────────────────────────────────────────
        User landlord1 = landlord("Mikias",  "Zeleke",  "landlord1@rentalpro.et", "0922000001", "Bole");
        User landlord2 = landlord("Hiwot",   "Bekele",  "landlord2@rentalpro.et", "0922000002", "Kirkos");
        User landlord3 = landlord("Solomon", "Tadesse", "landlord3@rentalpro.et", "0922000003", "Yeka");

        // ── Tenants ───────────────────────────────────────────────────────────
        User tenant1 = tenant("Berhane", "Alemu",   "tenant1@rentalpro.et", "0933000001", "Bole");
        User tenant2 = tenant("Meron",   "Tesfaye", "tenant2@rentalpro.et", "0933000002", "Kirkos");
        User tenant3 = tenant("Dawit",   "Worku",   "tenant3@rentalpro.et", "0933000003", "Yeka");

        // ── Bole Cluster — APARTMENT_BUILDING, RESIDENTIAL ───────────────────
        // 8 units with consistent 550–650 ETB/m² → clear statistical average,
        // low std-dev → benchmark engine has high confidence at Level 1.
        Property boleApt = property(landlord1,
                "Bole Luxury Apartments", "Bole Road, Near Atlas Hotel",
                "Bole", "03", "05", "Residential",
                PropertyType.APARTMENT_BUILDING, 2019,
                9.0054, 38.7636, 1200.0);

        // 6 "normal" units — market rate 550–650 ETB/m²
        RentalUnit bU1 = unit(boleApt, "A-101", 75.0,  1, 2);
        RentalUnit bU2 = unit(boleApt, "A-102", 80.0,  1, 2);
        RentalUnit bU3 = unit(boleApt, "A-201", 90.0,  2, 3);
        RentalUnit bU4 = unit(boleApt, "A-202", 85.0,  2, 3);
        RentalUnit bU5 = unit(boleApt, "A-301", 95.0,  3, 3);
        RentalUnit bU6 = unit(boleApt, "A-302", 100.0, 3, 3);
        // 1 large unit — UNDER-REPORTED anomaly (150m², declared 15,000 ETB = 100 ETB/m²)
        RentalUnit bU7 = unit(boleApt, "B-101", 150.0, 1, 4);
        // 1 medium unit — OVER-REPORTED anomaly (70m², declared 70,000 ETB = 1,000 ETB/m²)
        RentalUnit bU8 = unit(boleApt, "B-201", 70.0,  2, 2);

        // Active contracts for the 6 normal units
        RentalContract c1 = activeContract(landlord1, tenant1, bU1, 43750.0,  officerBole);  // 583 ETB/m²
        RentalContract c2 = activeContract(landlord1, tenant2, bU2, 48000.0,  officerBole);  // 600 ETB/m²
        RentalContract c3 = activeContract(landlord1, tenant3, bU3, 54000.0,  officerBole);  // 600 ETB/m²
        RentalContract c4 = activeContract(landlord2, tenant1, bU4, 46750.0,  officerBole);  // 550 ETB/m²
        RentalContract c5 = activeContract(landlord2, tenant2, bU5, 61750.0,  officerBole);  // 650 ETB/m²
        RentalContract c6 = activeContract(landlord2, tenant3, bU6, 65000.0,  officerBole);  // 650 ETB/m²

        // Anomaly contracts
        RentalContract cUnder = activeContract(landlord3, tenant1, bU7, 15000.0, officerBole); // 100 ETB/m² — HIGH UNDER
        RentalContract cOver  = activeContract(landlord3, tenant2, bU8, 70000.0, officerBole); // 1000 ETB/m² — HIGH OVER

        // Declarations for normal units — these build the comparison group
        LocalDate period = LocalDate.now().withDayOfMonth(1);
        declaration(c1, period, 43750.0,  600.0, 38800.0, 54000.0, 8, 1, 95.0, false, null,            null);
        declaration(c2, period, 48000.0,  600.0, 38800.0, 54000.0, 8, 1, 95.0, false, null,            null);
        declaration(c3, period, 54000.0,  600.0, 38800.0, 54000.0, 8, 1, 95.0, false, null,            null);
        declaration(c4, period, 46750.0,  600.0, 38800.0, 54000.0, 8, 1, 95.0, false, null,            null);
        declaration(c5, period, 61750.0,  600.0, 38800.0, 54000.0, 8, 1, 95.0, false, null,            null);
        declaration(c6, period, 65000.0,  600.0, 38800.0, 54000.0, 8, 1, 95.0, false, null,            null);

        // Anomaly declarations
        declaration(cUnder, period, 15000.0, 90000.0, 72000.0, 108000.0, 8, 1, 95.0, true,
                "UNDER_REPORTED", "HIGH");
        declaration(cOver,  period, 70000.0, 42000.0, 33600.0,  50400.0, 8, 1, 95.0, true,
                "OVER_REPORTED",  "HIGH");

        // ── Kirkos Cluster — MIXED_USE_BUILDING ──────────────────────────────
        Property kirkosApt = property(landlord2,
                "Kirkos Mixed-Use Tower", "Kirkos Main Street",
                "Kirkos", "08", "03", "Mixed",
                PropertyType.MIXED_USE_BUILDING, 2015,
                8.9950, 38.7550, 800.0);

        RentalUnit kU1 = unit(kirkosApt, "K-101", 60.0, 1, 2);
        RentalUnit kU2 = unit(kirkosApt, "K-201", 65.0, 2, 2);
        RentalUnit kU3 = unit(kirkosApt, "K-301", 70.0, 3, 3);

        RentalContract kC1 = activeContract(landlord2, tenant3, kU1, 27000.0, officerKirkos); // 450 ETB/m²
        RentalContract kC2 = activeContract(landlord3, tenant1, kU2, 29250.0, officerKirkos); // 450 ETB/m²
        RentalContract kC3 = activeContract(landlord1, tenant2, kU3, 33600.0, officerKirkos); // 480 ETB/m²

        declaration(kC1, period, 27000.0, 28800.0, 21600.0, 36000.0, 3, 3, 45.0, false, null, null);
        declaration(kC2, period, 29250.0, 28800.0, 21600.0, 36000.0, 3, 3, 45.0, false, null, null);
        declaration(kC3, period, 33600.0, 28800.0, 21600.0, 36000.0, 3, 3, 45.0, false, null, null);

        // ── Yeka Cluster — HOUSE ──────────────────────────────────────────────
        Property yekaHouse = property(landlord3,
                "Yeka Residential Compound", "Yeka Abado Road",
                "Yeka", "11", "07", "Residential",
                PropertyType.HOUSE, 2010,
                9.0450, 38.8100, 400.0);

        RentalUnit yU1 = unit(yekaHouse, "Y-01", 120.0, 1, 4);
        RentalUnit yU2 = unit(yekaHouse, "Y-02", 110.0, 1, 3);

        RentalContract yC1 = activeContract(landlord3, tenant3, yU1, 22800.0, officerBole); // 190 ETB/m²
        RentalContract yC2 = activeContract(landlord1, tenant2, yU2, 20900.0, officerBole); // 190 ETB/m²

        declaration(yC1, period, 22800.0, 22800.0, 17100.0, 28500.0, 2, 4, 30.0, false, null, null);
        declaration(yC2, period, 20900.0, 22800.0, 17100.0, 28500.0, 2, 4, 30.0, false, null, null);

        // ── Arada Cluster — COMMERCIAL_BUILDING ──────────────────────────────
        Property aradaCommercial = property(landlord1,
                "Arada Commercial Centre", "Piassa, Churchill Avenue",
                "Arada", "02", "01", "Commercial",
                PropertyType.COMMERCIAL_BUILDING, 2005,
                9.0350, 38.7450, 600.0);

        RentalUnit aU1 = unit(aradaCommercial, "C-01", 50.0, 1, 0);
        RentalUnit aU2 = unit(aradaCommercial, "C-02", 55.0, 1, 0);

        RentalContract aC1 = activeContract(landlord1, tenant1, aU1, 17500.0, officerBole); // 350 ETB/m²
        RentalContract aC2 = activeContract(landlord2, tenant3, aU2, 19250.0, officerBole); // 350 ETB/m²

        declaration(aC1, period, 17500.0, 17500.0, 13125.0, 21875.0, 2, 3, 25.0, false, null, null);
        declaration(aC2, period, 19250.0, 17500.0, 13125.0, 21875.0, 2, 3, 25.0, false, null, null);

        log.info("DatabaseSeeder: Demo dataset seeded — {} users, {} properties, {} contracts, {} declarations",
                userRepository.count(),
                propertyRepository.count(),
                contractRepository.count(),
                declarationRepository.count());
    }

    // =========================================================================
    // Builder helpers
    // =========================================================================

    private User officer(String first, String last, String email, String phone, String subCity) {
        return userRepository.save(User.builder()
                .firstName(first).lastName(last)
                .email(email).phoneNumber(phone)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .role(UserRole.SUBCITY_STAFF)
                .subCityZone(subCity)
                .isActive(true)
                .accountStatus(AccountStatus.VERIFIED)
                .entityType(EntityType.INDIVIDUAL)
                .build());
    }

    private User landlord(String first, String last, String email, String phone, String subCity) {
        return userRepository.save(User.builder()
                .firstName(first).lastName(last)
                .email(email).phoneNumber(phone)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .role(UserRole.LANDLORD)
                .subCityZone(subCity)
                .isActive(true)
                .accountStatus(AccountStatus.VERIFIED)
                .entityType(EntityType.INDIVIDUAL)
                .nationalIdNumber("FAN" + phone.substring(4))   // unique placeholder
                .tinNumber("TIN" + phone.substring(4))
                .residentialAddress("Addis Ababa, " + subCity)
                .dateOfBirth(java.time.LocalDate.of(1985, 1, 1))
                .build());
    }

    private User tenant(String first, String last, String email, String phone, String subCity) {
        return userRepository.save(User.builder()
                .firstName(first).lastName(last)
                .email(email).phoneNumber(phone)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .role(UserRole.TENANT)
                .subCityZone(subCity)
                .isActive(true)
                .accountStatus(AccountStatus.VERIFIED)
                .entityType(EntityType.INDIVIDUAL)
                .nationalIdNumber("FAN" + phone.substring(4))
                .tinNumber("TIN" + phone.substring(4))
                .residentialAddress("Addis Ababa, " + subCity)
                .dateOfBirth(java.time.LocalDate.of(1990, 6, 15))
                .build());
    }

    private Property property(User owner, String name, String address,
                               String subCity, String woreda, String kebele,
                               String siteDesig, PropertyType type, int yearBuilt,
                               double lat, double lng, double totalArea) {
        return propertyRepository.save(Property.builder()
                .owner(owner)
                .propertyName(name)
                .address(address)
                .subCity(subCity)
                .woreda(woreda)
                .kebele(kebele)
                .siteDesignation(siteDesig)
                .propertyType(type)
                .yearBuilt(yearBuilt)
                .latitude(lat)
                .longitude(lng)
                .totalArea(totalArea)
                .status(PropertyStatus.ACTIVE)
                .build());
    }

    private RentalUnit unit(Property property, String number, double area, int floor, int rooms) {
        return unitRepository.save(RentalUnit.builder()
                .property(property)
                .unitNumber(number)
                .floorArea(area)
                .floorLevel(floor)
                .numberOfRooms(rooms)
                .build());
    }

    private RentalContract activeContract(User landlord, User tenant, RentalUnit unit,
                                           double monthlyRent, User reviewedBy) {
        LocalDate start = LocalDate.now().minusMonths(6);
        LocalDate end   = start.plusYears(2);
        return contractRepository.save(RentalContract.builder()
                .landlord(landlord)
                .tenant(tenant)
                .rentalUnit(unit)
                .propertyAddress(unit.getProperty().getAddress())
                .monthlyRent(monthlyRent)
                .startDate(start)
                .endDate(end)
                .paymentFrequency("Monthly")
                .status(ContractStatus.ACTIVE)
                .tenantSignature("Demo Signature")
                .tenantConfirmedAt(LocalDateTime.now().minusMonths(5))
                .landlordSubmittedAt(LocalDateTime.now().minusMonths(6))
                .officerReviewedAt(LocalDateTime.now().minusMonths(5))
                .reviewedBy(reviewedBy)
                .currency("ETB")
                .build());
    }

    private void declaration(RentalContract contract, LocalDate period,
                              double declaredRent, double benchmarkRent,
                              double lowerBound, double upperBound,
                              int sampleSize, int fallbackLevel, double stdDev,
                              boolean isAnomaly, String direction, String severity) {

        double deviation = Math.abs(declaredRent - benchmarkRent) / benchmarkRent;
        double anomalyScore = Math.min(deviation / 0.30, 1.0);

        String reason = null;
        if (isAnomaly && direction != null) {
            reason = String.format(
                    "Declared rent ETB %.0f is %.1f%% %s the expected range of ETB %.0f–%.0f " +
                    "(based on %d comparable properties, Level %d match). Severity: %s.",
                    declaredRent, deviation * 100,
                    "UNDER_REPORTED".equals(direction) ? "below" : "above",
                    lowerBound, upperBound, sampleSize, fallbackLevel, severity);
        }

        // Simple tax estimate: 10% of annual gross (individual, no deduction)
        double annualGross = declaredRent * 12;
        double taxableIncome = annualGross;
        double annualTax = computeProgressiveTax(taxableIncome);
        double monthlyTax = annualTax / 12;

        declarationRepository.save(RentDeclaration.builder()
                .contract(contract)
                .declarationPeriod(period)
                .declaredRent(declaredRent)
                .aiBenchmarkRent(benchmarkRent)
                .benchmarkPricePerM2(benchmarkRent / contract.getRentalUnit().getFloorArea())
                .benchmarkLowerBound(lowerBound)
                .benchmarkUpperBound(upperBound)
                .benchmarkSampleSize(sampleSize)
                .benchmarkFallbackLevel(fallbackLevel)
                .benchmarkStdDev(stdDev)
                .anomalyScore(anomalyScore)
                .isAnomaly(isAnomaly)
                .anomalyReason(reason)
                .anomalySeverity(severity)
                .anomalyDirection(direction)
                .claimDeduction(false)
                .deductionApplied(false)
                .deductionAmount(0.0)
                .taxableAnnualIncome(taxableIncome)
                .annualTax(annualTax)
                .estimatedTax(monthlyTax)
                .effectiveTaxRate(annualGross > 0 ? annualTax / annualGross : 0)
                .taxRuleVersion("1395/2025")
                .taxCompliant(true)
                .isVerified(false)
                .build());
    }

    /** Simplified progressive tax per Proclamation 1395/2025 bands. */
    private double computeProgressiveTax(double taxableAnnual) {
        if (taxableAnnual <= 24_000)  return 0;
        if (taxableAnnual <= 48_000)  return taxableAnnual * 0.10 - 2_400;
        if (taxableAnnual <= 78_000)  return taxableAnnual * 0.20 - 6_000;
        if (taxableAnnual <= 120_000) return taxableAnnual * 0.25 - 9_900;
        if (taxableAnnual <= 168_000) return taxableAnnual * 0.30 - 15_900;
        return taxableAnnual * 0.30 - 15_900;
    }
}
