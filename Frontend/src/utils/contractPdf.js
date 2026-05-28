/**
 * Opens a formatted rental contract in a new browser window.
 * The user can print it or use "Save as PDF" from the browser print dialog.
 * No external PDF library required.
 */
export function openContractPrintView(contract) {
  const fmt = (n) => "ETB " + Number(n).toLocaleString();
  const fmtDate = (d) => d ? new Date(d).toLocaleDateString("en-GB", { day: "2-digit", month: "long", year: "numeric" }) : "—";

  const html = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Rental Contract — ${contract.propertyAddress}</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: "Times New Roman", serif; font-size: 12pt; color: #111; background: #fff; padding: 40px; max-width: 800px; margin: 0 auto; }
    h1 { text-align: center; font-size: 18pt; text-transform: uppercase; letter-spacing: 2px; margin-bottom: 4px; }
    .subtitle { text-align: center; font-size: 10pt; color: #555; margin-bottom: 30px; }
    .ref { text-align: right; font-size: 9pt; color: #777; margin-bottom: 20px; }
    h2 { font-size: 12pt; text-transform: uppercase; border-bottom: 1px solid #333; padding-bottom: 4px; margin: 24px 0 12px; letter-spacing: 1px; }
    .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px 30px; margin-bottom: 8px; }
    .field label { font-size: 9pt; color: #666; display: block; margin-bottom: 2px; }
    .field span { font-size: 11pt; font-weight: bold; }
    .full { grid-column: 1 / -1; }
    .clause-box { border: 1px solid #ccc; padding: 12px; border-radius: 4px; font-size: 10pt; line-height: 1.6; white-space: pre-wrap; }
    .signature-section { margin-top: 50px; display: grid; grid-template-columns: 1fr 1fr; gap: 40px; }
    .sig-block { border-top: 1px solid #333; padding-top: 8px; }
    .sig-block .name { font-weight: bold; font-size: 11pt; }
    .sig-block .role { font-size: 9pt; color: #555; }
    .sig-block .signed { font-size: 9pt; color: #333; margin-top: 4px; }
    .footer { margin-top: 40px; text-align: center; font-size: 8pt; color: #999; border-top: 1px solid #eee; padding-top: 10px; }
    .status-badge { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 9pt; font-weight: bold; background: #e0f2fe; color: #0369a1; }
    @media print {
      body { padding: 20px; }
      button { display: none; }
    }
  </style>
</head>
<body>
  <div style="text-align:center; margin-bottom: 6px;">
    <span style="font-size:10pt; color:#555;">Federal Democratic Republic of Ethiopia</span><br/>
    <span style="font-size:10pt; color:#555;">Addis Ababa City Administration</span>
  </div>
  <h1>Residential Rental Contract</h1>
  <p class="subtitle">Proclamation No. 1395/2025 — RentalPro ET Platform</p>
  <div class="ref">
    Contract ID: ${contract.id}<br/>
    Status: <span class="status-badge">${contract.status?.replace(/_/g, " ")}</span>
  </div>

  <h2>1. Parties</h2>
  <div class="grid">
    <div class="field"><label>Landlord (Lessor)</label><span>${contract.landlordName}</span></div>
    <div class="field"><label>Tenant (Lessee)</label><span>${contract.tenantName}</span></div>
    <div class="field"><label>Tenant Email</label><span>${contract.tenantEmail || "—"}</span></div>
  </div>

  <h2>2. Property</h2>
  <div class="grid">
    <div class="field"><label>Property Name</label><span>${contract.propertyName}</span></div>
    <div class="field"><label>Unit Number</label><span>${contract.unitNumber}</span></div>
    <div class="field full"><label>Address</label><span>${contract.propertyAddress}</span></div>
  </div>

  <h2>3. Contract Term</h2>
  <div class="grid">
    <div class="field"><label>Start Date</label><span>${fmtDate(contract.startDate)}</span></div>
    <div class="field"><label>End Date</label><span>${fmtDate(contract.endDate)}</span></div>
    <div class="field"><label>Notice Period</label><span>${contract.noticePeriodDays || 30} days</span></div>
    <div class="field"><label>Renewal Terms</label><span>${contract.renewalType?.replace(/_/g, " ") || "Renegotiate"}</span></div>
  </div>

  <h2>4. Financial Terms</h2>
  <div class="grid">
    <div class="field"><label>Monthly Rent</label><span>${fmt(contract.monthlyRent)} (${contract.currency || "ETB"})</span></div>
    <div class="field"><label>Payment Frequency</label><span>${contract.paymentFrequency || "Monthly"}</span></div>
    <div class="field"><label>Payment Due Day</label><span>Day ${contract.paymentDueDay || 1} of each month</span></div>
    <div class="field"><label>Payment Method</label><span>${contract.paymentMethod?.replace(/_/g, " ") || "Bank Transfer"}</span></div>
    <div class="field"><label>Security Deposit</label><span>${contract.securityDepositAmount ? fmt(contract.securityDepositAmount) : "None"}</span></div>
  </div>

  ${contract.additionalClauses ? `
  <h2>5. Additional Clauses</h2>
  <div class="clause-box">${contract.additionalClauses}</div>
  ` : ""}

  <h2>${contract.additionalClauses ? "6" : "5"}. Signatures</h2>
  <div class="signature-section">
    <div class="sig-block">
      <div class="name">${contract.landlordName}</div>
      <div class="role">Landlord (Lessor)</div>
      ${contract.landlordSubmittedAt ? `<div class="signed">Submitted: ${fmtDate(contract.landlordSubmittedAt)}</div>` : ""}
    </div>
    <div class="sig-block">
      <div class="name">${contract.tenantName}</div>
      <div class="role">Tenant (Lessee)</div>
      ${contract.tenantSignature && contract.tenantSignature.startsWith("data:image")
        ? `<img src="${contract.tenantSignature}" style="max-height:60px; margin-top:6px; border:1px solid #eee; border-radius:4px;" alt="Tenant signature" />`
        : contract.tenantSignature
          ? `<div class="signed">Digital Signature: ${contract.tenantSignature}</div>`
          : ""}
      ${contract.tenantConfirmedAt ? `<div class="signed">Confirmed: ${fmtDate(contract.tenantConfirmedAt)}</div>` : ""}
    </div>
  </div>

  ${contract.reviewedByName ? `
  <div style="margin-top: 30px; padding: 12px; background: #f0fdf4; border: 1px solid #86efac; border-radius: 4px;">
    <strong>Officer Approval</strong><br/>
    Reviewed by: ${contract.reviewedByName}<br/>
    ${contract.officerReviewedAt ? `Date: ${fmtDate(contract.officerReviewedAt)}` : ""}
  </div>
  ` : ""}

  <div class="footer">
    Generated by RentalPro ET — ${new Date().toLocaleString()} — This document is an official record under Proclamation No. 1395/2025
  </div>

  <div style="text-align:center; margin-top: 20px;">
    <button onclick="window.print()" style="padding: 10px 30px; background: #1e3a5f; color: white; border: none; border-radius: 6px; font-size: 12pt; cursor: pointer;">
      Print / Save as PDF
    </button>
  </div>
</body>
</html>`;

  const win = window.open("", "_blank");
  win.document.write(html);
  win.document.close();
}
