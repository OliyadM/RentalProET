package com.rentalpro.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.rentalpro.model.entity.RentalContract;
import com.rentalpro.model.entity.User;
import com.rentalpro.service.ContractPdfService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class ContractPdfServiceImpl implements ContractPdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @Override
    public byte[] generateContractPdf(RentalContract contract) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
        
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font titleAmharicFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font articleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            // Header - Title in Amharic and English
            Paragraph titleAmharic = new Paragraph("የቤት ኪራይ ውል", titleAmharicFont);
            titleAmharic.setAlignment(Element.ALIGN_CENTER);
            doc.add(titleAmharic);
            
            Paragraph titleEnglish = new Paragraph("RENTAL AGREEMENT", titleFont);
            titleEnglish.setAlignment(Element.ALIGN_CENTER);
            doc.add(titleEnglish);
            
            doc.add(new Paragraph(" "));
            
            // Contract metadata
            Paragraph metadata = new Paragraph();
            metadata.add(new Chunk("Contract No: ", boldFont));
            metadata.add(new Chunk(contract.getId().toString(), normalFont));
            metadata.add(Chunk.NEWLINE);
            metadata.add(new Chunk("Date of Agreement: ", boldFont));
            metadata.add(new Chunk(LocalDate.now().format(DATE_FMT), normalFont));
            metadata.add(Chunk.NEWLINE);
            metadata.add(new Chunk("Jurisdiction: ", boldFont));
            metadata.add(new Chunk(contract.getRentalUnit().getProperty().getSubCity() + " Sub-City", normalFont));
            doc.add(metadata);
            
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("_________________________________________________________________________"));
            doc.add(new Paragraph(" "));

            // ARTICLE 1 - PARTIES
            addArticle(doc, "ARTICLE 1 — PARTIES (አካላት)", articleFont);
            
            User landlord = contract.getLandlord();
            User tenant = contract.getTenant();
            
            Paragraph landlordSection = new Paragraph();
            landlordSection.add(new Chunk("Landlord (አከራይ):", boldFont));
            landlordSection.add(Chunk.NEWLINE);
            landlordSection.add(new Chunk("  Name: ", normalFont));
            landlordSection.add(new Chunk(landlord.getFirstName() + " " + landlord.getLastName(), boldFont));
            landlordSection.add(Chunk.NEWLINE);
            landlordSection.add(new Chunk("  National ID: ", normalFont));
            landlordSection.add(new Chunk(safe(landlord.getNationalIdNumber()), normalFont));
            landlordSection.add(Chunk.NEWLINE);
            landlordSection.add(new Chunk("  TIN Number: ", normalFont));
            landlordSection.add(new Chunk(safe(landlord.getTinNumber()), normalFont));
            landlordSection.add(Chunk.NEWLINE);
            landlordSection.add(new Chunk("  Phone: ", normalFont));
            landlordSection.add(new Chunk(landlord.getPhoneNumber(), normalFont));
            landlordSection.add(Chunk.NEWLINE);
            landlordSection.add(new Chunk("  Address: ", normalFont));
            landlordSection.add(new Chunk(safe(landlord.getResidentialAddress()), normalFont));
            doc.add(landlordSection);
            
            doc.add(new Paragraph(" "));
            
            Paragraph tenantSection = new Paragraph();
            tenantSection.add(new Chunk("Tenant (ተከራይ):", boldFont));
            tenantSection.add(Chunk.NEWLINE);
            tenantSection.add(new Chunk("  Name: ", normalFont));
            tenantSection.add(new Chunk(tenant.getFirstName() + " " + tenant.getLastName(), boldFont));
            tenantSection.add(Chunk.NEWLINE);
            tenantSection.add(new Chunk("  National ID: ", normalFont));
            tenantSection.add(new Chunk(safe(tenant.getNationalIdNumber()), normalFont));
            tenantSection.add(Chunk.NEWLINE);
            tenantSection.add(new Chunk("  Phone: ", normalFont));
            tenantSection.add(new Chunk(tenant.getPhoneNumber(), normalFont));
            doc.add(tenantSection);
            
            doc.add(new Paragraph(" "));

            // ARTICLE 2 - PROPERTY DESCRIPTION
            addArticle(doc, "ARTICLE 2 — PROPERTY DESCRIPTION (የንብረት መግለጫ)", articleFont);
            
            Paragraph propertySection = new Paragraph();
            propertySection.add(new Chunk("  Property Name: ", normalFont));
            propertySection.add(new Chunk(contract.getRentalUnit().getProperty().getPropertyName(), boldFont));
            propertySection.add(Chunk.NEWLINE);
            propertySection.add(new Chunk("  Address: ", normalFont));
            propertySection.add(new Chunk(contract.getPropertyAddress(), normalFont));
            propertySection.add(Chunk.NEWLINE);
            propertySection.add(new Chunk("  Sub-City: ", normalFont));
            propertySection.add(new Chunk(contract.getRentalUnit().getProperty().getSubCity(), normalFont));
            propertySection.add(Chunk.NEWLINE);
            propertySection.add(new Chunk("  Woreda: ", normalFont));
            propertySection.add(new Chunk(contract.getRentalUnit().getProperty().getWoreda(), normalFont));
            propertySection.add(Chunk.NEWLINE);
            propertySection.add(new Chunk("  Kebele: ", normalFont));
            propertySection.add(new Chunk(contract.getRentalUnit().getProperty().getKebele(), normalFont));
            propertySection.add(Chunk.NEWLINE);
            propertySection.add(new Chunk("  Property Type: ", normalFont));
            propertySection.add(new Chunk(contract.getRentalUnit().getProperty().getPropertyType().toString().replace("_", " "), normalFont));
            propertySection.add(Chunk.NEWLINE);
            propertySection.add(new Chunk("  Unit Number: ", normalFont));
            propertySection.add(new Chunk(contract.getRentalUnit().getUnitNumber(), boldFont));
            propertySection.add(Chunk.NEWLINE);
            propertySection.add(new Chunk("  Floor Level: ", normalFont));
            propertySection.add(new Chunk(safe(contract.getRentalUnit().getFloorLevel()), normalFont));
            propertySection.add(Chunk.NEWLINE);
            propertySection.add(new Chunk("  Floor Area: ", normalFont));
            propertySection.add(new Chunk(safe(contract.getRentalUnit().getFloorArea()) + " m²", normalFont));
            propertySection.add(Chunk.NEWLINE);
            propertySection.add(new Chunk("  Number of Rooms: ", normalFont));
            propertySection.add(new Chunk(safe(contract.getRentalUnit().getNumberOfRooms()), normalFont));
            doc.add(propertySection);
            
            doc.add(new Paragraph(" "));

            // ARTICLE 3 - TERM
            addArticle(doc, "ARTICLE 3 — TERM (የውል ጊዜ)", articleFont);
            
            long durationMonths = ChronoUnit.MONTHS.between(contract.getStartDate(), contract.getEndDate());
            
            Paragraph termSection = new Paragraph();
            termSection.add(new Chunk("  Start Date: ", normalFont));
            termSection.add(new Chunk(contract.getStartDate().format(DATE_FMT), boldFont));
            termSection.add(Chunk.NEWLINE);
            termSection.add(new Chunk("  End Date: ", normalFont));
            termSection.add(new Chunk(contract.getEndDate().format(DATE_FMT), boldFont));
            termSection.add(Chunk.NEWLINE);
            termSection.add(new Chunk("  Duration: ", normalFont));
            termSection.add(new Chunk(durationMonths + " months", normalFont));
            doc.add(termSection);
            
            doc.add(new Paragraph(" "));

            // ARTICLE 4 - RENT AND PAYMENT
            addArticle(doc, "ARTICLE 4 — RENT AND PAYMENT (ኪራይ እና ክፍያ)", articleFont);
            
            Paragraph paymentSection = new Paragraph();
            paymentSection.add(new Chunk("  Monthly Rent: ", normalFont));
            paymentSection.add(new Chunk(formatMoney(contract.getMonthlyRent()) + " ETB", boldFont));
            paymentSection.add(Chunk.NEWLINE);
            paymentSection.add(new Chunk("  (In words: ", smallFont));
            paymentSection.add(new Chunk(numberToWords(contract.getMonthlyRent()) + " Birr)", smallFont));
            paymentSection.add(Chunk.NEWLINE);
            paymentSection.add(new Chunk("  Payment Frequency: ", normalFont));
            paymentSection.add(new Chunk(contract.getPaymentFrequency(), normalFont));
            paymentSection.add(Chunk.NEWLINE);
            paymentSection.add(new Chunk("  Payment Due Date: ", normalFont));
            paymentSection.add(new Chunk(contract.getPaymentDueDay() + " of each month", boldFont));
            paymentSection.add(Chunk.NEWLINE);
            paymentSection.add(new Chunk("  Payment Method: ", normalFont));
            paymentSection.add(new Chunk(contract.getPaymentMethod().replace("_", " "), normalFont));
            doc.add(paymentSection);
            
            doc.add(new Paragraph(" "));

            // ARTICLE 5 - SECURITY DEPOSIT
            addArticle(doc, "ARTICLE 5 — SECURITY DEPOSIT (ዋስትና ተቀማጭ)", articleFont);
            
            Paragraph depositSection = new Paragraph();
            if (contract.getSecurityDepositAmount() != null && contract.getSecurityDepositAmount() > 0) {
                depositSection.add(new Chunk("  Deposit Amount: ", normalFont));
                depositSection.add(new Chunk(formatMoney(contract.getSecurityDepositAmount()) + " ETB", boldFont));
                depositSection.add(Chunk.NEWLINE);
                depositSection.add(new Chunk("  The security deposit shall be returned to the Tenant within 30 days after the end of the tenancy, ", normalFont));
                depositSection.add(new Chunk("less any deductions for damages beyond normal wear and tear or unpaid rent.", normalFont));
            } else {
                depositSection.add(new Chunk("  No security deposit required for this contract.", normalFont));
            }
            doc.add(depositSection);
            
            doc.add(new Paragraph(" "));

            // ARTICLE 6 - OBLIGATIONS OF LANDLORD
            addArticle(doc, "ARTICLE 6 — OBLIGATIONS OF LANDLORD (የአከራይ ግዴታዎች)", articleFont);
            
            Paragraph landlordObligations = new Paragraph();
            landlordObligations.add(new Chunk("  The Landlord shall:", normalFont));
            landlordObligations.add(Chunk.NEWLINE);
            landlordObligations.add(new Chunk("  • Deliver the property in good and habitable condition", normalFont));
            landlordObligations.add(Chunk.NEWLINE);
            landlordObligations.add(new Chunk("  • Maintain the structural integrity of the property", normalFont));
            landlordObligations.add(Chunk.NEWLINE);
            landlordObligations.add(new Chunk("  • Make necessary repairs to keep the property habitable", normalFont));
            landlordObligations.add(Chunk.NEWLINE);
            landlordObligations.add(new Chunk("  • Respect the Tenant's right to quiet enjoyment", normalFont));
            landlordObligations.add(Chunk.NEWLINE);
            landlordObligations.add(new Chunk("  • Provide reasonable notice before entering the property", normalFont));
            doc.add(landlordObligations);
            
            doc.add(new Paragraph(" "));

            // ARTICLE 7 - OBLIGATIONS OF TENANT
            addArticle(doc, "ARTICLE 7 — OBLIGATIONS OF TENANT (የተከራይ ግዴታዎች)", articleFont);
            
            Paragraph tenantObligations = new Paragraph();
            tenantObligations.add(new Chunk("  The Tenant shall:", normalFont));
            tenantObligations.add(Chunk.NEWLINE);
            tenantObligations.add(new Chunk("  • Pay rent on time as specified in Article 4", normalFont));
            tenantObligations.add(Chunk.NEWLINE);
            tenantObligations.add(new Chunk("  • Use the property for residential purposes only", normalFont));
            tenantObligations.add(Chunk.NEWLINE);
            tenantObligations.add(new Chunk("  • Maintain the property in good condition", normalFont));
            tenantObligations.add(Chunk.NEWLINE);
            tenantObligations.add(new Chunk("  • Not sublet the property without written consent from the Landlord", normalFont));
            tenantObligations.add(Chunk.NEWLINE);
            tenantObligations.add(new Chunk("  • Not make structural alterations without written consent", normalFont));
            tenantObligations.add(Chunk.NEWLINE);
            tenantObligations.add(new Chunk("  • Pay for utilities as agreed", normalFont));
            doc.add(tenantObligations);
            
            doc.add(new Paragraph(" "));

            // ARTICLE 8 - TERMINATION
            addArticle(doc, "ARTICLE 8 — TERMINATION (ውሉን ማቋረጥ)", articleFont);
            
            Paragraph terminationSection = new Paragraph();
            terminationSection.add(new Chunk("  Notice Period: ", normalFont));
            terminationSection.add(new Chunk(contract.getNoticePeriodDays() + " days", boldFont));
            terminationSection.add(Chunk.NEWLINE);
            terminationSection.add(new Chunk("  Either party may terminate this agreement by providing written notice of ", normalFont));
            terminationSection.add(new Chunk(contract.getNoticePeriodDays() + " days ", boldFont));
            terminationSection.add(new Chunk("to the other party.", normalFont));
            terminationSection.add(Chunk.NEWLINE);
            terminationSection.add(new Chunk("  Early termination may result in forfeiture of the security deposit or additional penalties as per Ethiopian law.", normalFont));
            doc.add(terminationSection);
            
            doc.add(new Paragraph(" "));

            // ARTICLE 9 - RENEWAL
            addArticle(doc, "ARTICLE 9 — RENEWAL (ማደስ)", articleFont);
            
            Paragraph renewalSection = new Paragraph();
            renewalSection.add(new Chunk("  Renewal Type: ", normalFont));
            renewalSection.add(new Chunk(contract.getRenewalType().replace("_", " "), boldFont));
            renewalSection.add(Chunk.NEWLINE);
            
            String renewalText = switch (contract.getRenewalType()) {
                case "AUTO_RENEW" -> "This contract will automatically renew for successive terms unless either party provides notice of termination.";
                case "FIXED_TERM" -> "This contract is for a fixed term only and will not be renewed automatically. A new agreement must be negotiated.";
                default -> "At the end of the term, both parties may renegotiate the terms for a new rental period.";
            };
            renewalSection.add(new Chunk("  " + renewalText, normalFont));
            doc.add(renewalSection);
            
            doc.add(new Paragraph(" "));

            // ARTICLE 10 - GOVERNING LAW
            addArticle(doc, "ARTICLE 10 — GOVERNING LAW (አስተዳደር ህግ)", articleFont);
            
            Paragraph lawSection = new Paragraph();
            lawSection.add(new Chunk("  This contract is governed by the laws of the Federal Democratic Republic of Ethiopia.", normalFont));
            lawSection.add(Chunk.NEWLINE);
            lawSection.add(new Chunk("  Any disputes arising from this contract shall be resolved through mediation, and if unresolved, ", normalFont));
            lawSection.add(new Chunk("through the courts of " + contract.getRentalUnit().getProperty().getSubCity() + " Sub-City jurisdiction.", normalFont));
            doc.add(lawSection);
            
            doc.add(new Paragraph(" "));

            // ADDITIONAL CLAUSES
            if (contract.getAdditionalClauses() != null && !contract.getAdditionalClauses().isBlank()) {
                addArticle(doc, "ARTICLE 11 — ADDITIONAL TERMS (ተጨማሪ ውሎች)", articleFont);
                Paragraph additionalSection = new Paragraph("  " + contract.getAdditionalClauses(), normalFont);
                doc.add(additionalSection);
                doc.add(new Paragraph(" "));
            }

            // SIGNATURES
            doc.add(new Paragraph("_________________________________________________________________________"));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));
            
            Paragraph signaturesTitle = new Paragraph("SIGNATURES (ፊርማዎች)", articleFont);
            signaturesTitle.setAlignment(Element.ALIGN_CENTER);
            doc.add(signaturesTitle);
            doc.add(new Paragraph(" "));
            
            // Signature table
            PdfPTable sigTable = new PdfPTable(2);
            sigTable.setWidthPercentage(100);
            
            // Landlord signature
            PdfPCell landlordSigCell = new PdfPCell();
            landlordSigCell.setBorder(Rectangle.NO_BORDER);
            landlordSigCell.setPadding(10);
            Paragraph landlordSig = new Paragraph();
            landlordSig.add(new Chunk("Landlord (አከራይ):", boldFont));
            landlordSig.add(Chunk.NEWLINE);
            landlordSig.add(Chunk.NEWLINE);
            landlordSig.add(new Chunk("_________________________", normalFont));
            landlordSig.add(Chunk.NEWLINE);
            landlordSig.add(new Chunk(landlord.getFirstName() + " " + landlord.getLastName(), normalFont));
            landlordSig.add(Chunk.NEWLINE);
            landlordSig.add(new Chunk("Date: _________________", normalFont));
            landlordSigCell.addElement(landlordSig);
            sigTable.addCell(landlordSigCell);
            
            // Tenant signature
            PdfPCell tenantSigCell = new PdfPCell();
            tenantSigCell.setBorder(Rectangle.NO_BORDER);
            tenantSigCell.setPadding(10);
            Paragraph tenantSig = new Paragraph();
            tenantSig.add(new Chunk("Tenant (ተከራይ):", boldFont));
            tenantSig.add(Chunk.NEWLINE);
            tenantSig.add(Chunk.NEWLINE);
            tenantSig.add(new Chunk("_________________________", normalFont));
            tenantSig.add(Chunk.NEWLINE);
            tenantSig.add(new Chunk(tenant.getFirstName() + " " + tenant.getLastName(), normalFont));
            tenantSig.add(Chunk.NEWLINE);
            tenantSig.add(new Chunk("Date: _________________", normalFont));
            tenantSigCell.addElement(tenantSig);
            sigTable.addCell(tenantSigCell);
            
            doc.add(sigTable);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));
            
            // Witness signatures
            PdfPTable witnessTable = new PdfPTable(2);
            witnessTable.setWidthPercentage(100);
            
            PdfPCell witness1Cell = new PdfPCell();
            witness1Cell.setBorder(Rectangle.NO_BORDER);
            witness1Cell.setPadding(10);
            Paragraph witness1 = new Paragraph();
            witness1.add(new Chunk("Witness 1:", boldFont));
            witness1.add(Chunk.NEWLINE);
            witness1.add(Chunk.NEWLINE);
            witness1.add(new Chunk("_________________________", normalFont));
            witness1.add(Chunk.NEWLINE);
            witness1.add(new Chunk("Name: ___________________", normalFont));
            witness1.add(Chunk.NEWLINE);
            witness1.add(new Chunk("Date: ___________________", normalFont));
            witness1Cell.addElement(witness1);
            witnessTable.addCell(witness1Cell);
            
            PdfPCell witness2Cell = new PdfPCell();
            witness2Cell.setBorder(Rectangle.NO_BORDER);
            witness2Cell.setPadding(10);
            Paragraph witness2 = new Paragraph();
            witness2.add(new Chunk("Witness 2:", boldFont));
            witness2.add(Chunk.NEWLINE);
            witness2.add(Chunk.NEWLINE);
            witness2.add(new Chunk("_________________________", normalFont));
            witness2.add(Chunk.NEWLINE);
            witness2.add(new Chunk("Name: ___________________", normalFont));
            witness2.add(Chunk.NEWLINE);
            witness2.add(new Chunk("Date: ___________________", normalFont));
            witness2Cell.addElement(witness2);
            witnessTable.addCell(witness2Cell);
            
            doc.add(witnessTable);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));
            
            // Officer approval section
            Paragraph officerSection = new Paragraph();
            officerSection.add(new Chunk("Officer Approval:", boldFont));
            officerSection.add(Chunk.NEWLINE);
            officerSection.add(Chunk.NEWLINE);
            officerSection.add(new Chunk("Signature: _________________________", normalFont));
            officerSection.add(Chunk.NEWLINE);
            officerSection.add(new Chunk("Name: _____________________________", normalFont));
            officerSection.add(Chunk.NEWLINE);
            officerSection.add(new Chunk("Date: _____________________________", normalFont));
            officerSection.add(Chunk.NEWLINE);
            officerSection.add(Chunk.NEWLINE);
            officerSection.add(new Chunk("[Official Stamp/Seal]", smallFont));
            doc.add(officerSection);
            
        } catch (DocumentException ex) {
            throw new RuntimeException("Failed to generate contract PDF", ex);
        } finally {
            doc.close();
        }
        
        return out.toByteArray();
    }
    
    private void addArticle(Document doc, String title, Font font) throws DocumentException {
        Paragraph article = new Paragraph(title, font);
        doc.add(article);
        doc.add(new Paragraph(" "));
    }
    
    private String safe(Object value) {
        return value == null ? "N/A" : String.valueOf(value);
    }
    
    private String formatMoney(Double amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }
    
    private String numberToWords(Double amount) {
        if (amount == null) return "Zero";
        // Simple implementation - can be enhanced
        long intPart = amount.longValue();
        return convertNumberToWords(intPart);
    }
    
    private String convertNumberToWords(long number) {
        if (number == 0) return "Zero";
        
        String[] ones = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
        String[] teens = {"Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
        
        if (number < 10) return ones[(int)number];
        if (number < 20) return teens[(int)(number - 10)];
        if (number < 100) return tens[(int)(number / 10)] + (number % 10 > 0 ? " " + ones[(int)(number % 10)] : "");
        if (number < 1000) return ones[(int)(number / 100)] + " Hundred" + (number % 100 > 0 ? " and " + convertNumberToWords(number % 100) : "");
        if (number < 1000000) return convertNumberToWords(number / 1000) + " Thousand" + (number % 1000 > 0 ? " " + convertNumberToWords(number % 1000) : "");
        
        return String.valueOf(number); // Fallback for very large numbers
    }
}
