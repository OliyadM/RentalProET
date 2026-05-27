package com.rentalpro.controller;

import com.rentalpro.model.dto.response.RentDeclarationResponse;
import com.rentalpro.model.entity.User;
import com.rentalpro.repository.UserRepository;
import com.rentalpro.service.RentDeclarationService;
import com.rentalpro.service.impl.TaxReportPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/declarations")
@RequiredArgsConstructor
public class RentDeclarationController {

    private final RentDeclarationService declarationService;
    private final UserRepository userRepository;
    private final TaxReportPdfService taxReportPdfService;

    @PostMapping("/contract/{contractId}")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<RentDeclarationResponse> createDeclaration(
            @PathVariable UUID contractId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period,
            @RequestParam Double declaredRent,
            @RequestParam(defaultValue = "false") boolean claimDeduction) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(declarationService.createDeclaration(
                contractId, period, declaredRent, currentUser.getId(), claimDeduction));
    }

    @GetMapping("/contract/{contractId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RentDeclarationResponse>> getByContract(@PathVariable UUID contractId) {
        return ResponseEntity.ok(declarationService.getDeclarationsByContract(contractId));
    }

    @GetMapping("/anomalies/{subCity}")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<List<RentDeclarationResponse>> getAnomalies(@PathVariable String subCity) {
        return ResponseEntity.ok(declarationService.getAnomalies(subCity));
    }
    @GetMapping("/unverified")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<List<RentDeclarationResponse>> getUnverified(@RequestParam String subCity) {
        return ResponseEntity.ok(declarationService.getUnverifiedDeclarations(subCity));
    }

    @GetMapping(value = "/{declarationId}/tax-summary.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadTaxSummaryPdf(@PathVariable UUID declarationId) {
        RentDeclarationResponse declaration = declarationService.getDeclarationById(declarationId);
        byte[] pdf = taxReportPdfService.buildDeclarationTaxSummaryPdf(declaration);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tax-summary-" + declarationId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/compliance-report.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<byte[]> downloadComplianceReportPdf(
            @RequestParam String subCity,
            @RequestParam(defaultValue = "ALL") String filter) {

        String normalizedFilter = filter.toUpperCase();
        List<RentDeclarationResponse> declarations;
        switch (normalizedFilter) {
            case "ANOMALIES" -> declarations = declarationService.getAnomalies(subCity);
            case "UNVERIFIED" -> declarations = declarationService.getUnverifiedDeclarations(subCity);
            default -> declarations = declarationService.getUnverifiedDeclarations(subCity);
        }

        byte[] pdf = taxReportPdfService.buildComplianceReportPdf(declarations, subCity, normalizedFilter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=compliance-report-" + subCity + "-" + normalizedFilter + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PutMapping("/{declarationId}/verify")
    @PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<RentDeclarationResponse> verifyDeclaration(
            @PathVariable UUID declarationId,
            @RequestParam(required = false) String notes) {

        User currentUser = getCurrentUser();
        return ResponseEntity.ok(declarationService.verifyDeclaration(declarationId, notes, currentUser.getId()));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}