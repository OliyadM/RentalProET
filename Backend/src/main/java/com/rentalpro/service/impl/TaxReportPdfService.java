package com.rentalpro.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.rentalpro.model.dto.response.RentDeclarationResponse;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class TaxReportPdfService {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] buildDeclarationTaxSummaryPdf(RentDeclarationResponse d) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font subtitle = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font section = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            doc.add(new Paragraph("Rental Tax Summary (Advisory)", title));
            doc.add(new Paragraph("Generated: " + LocalDateTime.now().format(TS_FMT), subtitle));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Declaration", section));
            PdfPTable declarationTable = new PdfPTable(2);
            declarationTable.setWidthPercentage(100);
            addRow(declarationTable, "Declaration ID", safe(d.getId()));
            addRow(declarationTable, "Contract ID", safe(d.getContractId()));
            addRow(declarationTable, "Period", safe(d.getDeclarationPeriod()));
            addRow(declarationTable, "Declared Monthly Rent", money(d.getDeclaredRent()));
            addRow(declarationTable, "AI Benchmark Rent", money(d.getAiBenchmarkRent()));
            addRow(declarationTable, "Tax Rule Version", safe(d.getTaxRuleVersion()));
            addRow(declarationTable, "Mixed-use Warning", boolLabel(d.getMixedUseDeductionWarning()));
            doc.add(declarationTable);
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Tax Breakdown", section));
            PdfPTable taxTable = new PdfPTable(2);
            taxTable.setWidthPercentage(100);
            addRow(taxTable, "Estimated Tax (Monthly)", money(d.getEstimatedTax()));
            addRow(taxTable, "Annual Tax", money(d.getAnnualTax()));
            addRow(taxTable, "Taxable Annual Income", money(d.getTaxableAnnualIncome()));
            addRow(taxTable, "Deduction Applied", boolLabel(d.getDeductionApplied()));
            addRow(taxTable, "Deduction Amount", money(d.getDeductionAmount()));
            addRow(taxTable, "Effective Tax Rate", percent(d.getEffectiveTaxRate()));
            doc.add(taxTable);
            doc.add(new Paragraph(" "));

            String advisory = d.getTaxAdvisoryNote() != null && !d.getTaxAdvisoryNote().isBlank()
                    ? d.getTaxAdvisoryNote()
                    : "This estimate covers rental income only. Total tax liability may differ if landlord has other income sources.";
            doc.add(new Paragraph("Advisory: " + advisory, subtitle));
            doc.add(new Paragraph("For compliance screening and taxpayer guidance only.", subtitle));
        } catch (DocumentException ex) {
            throw new RuntimeException("Failed to generate tax summary PDF", ex);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    public byte[] buildComplianceReportPdf(List<RentDeclarationResponse> declarations, String subCity, String filter) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);
            Font subtitle = FontFactory.getFont(FontFactory.HELVETICA, 10);

            doc.add(new Paragraph("Officer Compliance Report (Advisory)", title));
            doc.add(new Paragraph("Sub-city: " + subCity + "    Filter: " + filter, subtitle));
            doc.add(new Paragraph("Generated: " + LocalDateTime.now().format(TS_FMT), subtitle));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(new float[]{2.4f, 1.4f, 1.4f, 1.4f, 1.2f, 1.4f, 1.2f, 1.6f, 1.2f});
            table.setWidthPercentage(100);
            addHeader(table, "Contract");
            addHeader(table, "Period");
            addHeader(table, "Declared");
            addHeader(table, "Benchmark");
            addHeader(table, "Anomaly");
            addHeader(table, "Tax/Month");
            addHeader(table, "Tax/Year");
            addHeader(table, "Tax Rule");
            addHeader(table, "Verified");

            for (RentDeclarationResponse d : declarations) {
                table.addCell(cell(safe(d.getContractId())));
                table.addCell(cell(safe(d.getDeclarationPeriod())));
                table.addCell(cell(money(d.getDeclaredRent())));
                table.addCell(cell(money(d.getAiBenchmarkRent())));
                table.addCell(cell(Boolean.TRUE.equals(d.getIsAnomaly()) ? "Flagged" : "Clean"));
                table.addCell(cell(money(d.getEstimatedTax())));
                table.addCell(cell(money(d.getAnnualTax())));
                table.addCell(cell(safe(d.getTaxRuleVersion())));
                table.addCell(cell(Boolean.TRUE.equals(d.getIsVerified()) ? "Yes" : "No"));
            }

            doc.add(table);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Note: Tax values are advisory estimates based on declared rental income only.", subtitle));
        } catch (DocumentException ex) {
            throw new RuntimeException("Failed to generate compliance report PDF", ex);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    private void addHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addRow(PdfPTable table, String label, String value) {
        table.addCell(cell(label));
        table.addCell(cell(value));
    }

    private PdfPCell cell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "N/A" : text));
        cell.setPadding(6);
        return cell;
    }

    private String safe(Object v) {
        return v == null ? "N/A" : String.valueOf(v);
    }

    private String money(Double amount) {
        if (amount == null) {
            return "N/A";
        }
        return "ETB " + String.format(Locale.US, "%,.2f", amount);
    }

    private String percent(Double ratio) {
        if (ratio == null) {
            return "N/A";
        }
        return String.format(Locale.US, "%.2f%%", ratio * 100.0);
    }

    private String boolLabel(Boolean value) {
        if (value == null) {
            return "N/A";
        }
        return value ? "Yes" : "No";
    }
}
