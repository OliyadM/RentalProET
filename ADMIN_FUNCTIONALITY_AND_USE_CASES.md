# Admin Functionality & Complete Use Cases

## Admin Configurable Settings

The Administrator can configure the following system-wide parameters:

### 1. **Rent Tax Rate (%)**
- **Purpose:** Applied to declared monthly rent to calculate estimated tax
- **Range:** 0% - 50%
- **Default:** (Check system config)
- **Impact:** Affects all new rent declarations and tax calculations
- **Note:** Existing records are NOT retroactively recalculated

### 2. **Anomaly Detection Threshold (%)**
- **Purpose:** Flags declarations that deviate from AI benchmark
- **Range:** 1% - 100%
- **Default:** (Check system config)
- **How it works:** If declared rent differs from AI benchmark by more than this %, it's flagged as anomaly
- **Example:** If threshold is 20% and AI benchmark is 10,000 ETB:
  - Declared rent < 8,000 ETB → Anomaly (under-declaration)
  - Declared rent > 12,000 ETB → Anomaly (over-declaration)

### 3. **Maximum Rent Increase Cap (%)**
- **Purpose:** Limits rent increases between contract renewals
- **Range:** 0% - 100%
- **Default:** (Check system config)
- **Example:** If cap is 10%:
  - Previous rent: 10,000 ETB
  - Maximum new rent: 11,000 ETB

---

## Admin Capabilities

### Officer Management
1. **Register New Officers**
   - Create officer accounts
   - Assign to specific sub-cities
   - Set initial passwords
   
2. **Activate/Deactivate Officers**
   - Enable or disable officer accounts
   - Prevents deactivated officers from logging in

3. **View Officer Directory**
   - See all registered officers
   - View assigned sub-cities
   - Check active/inactive status

---

## Complete System Use Cases

### **Use Case 1: Landlord Registration & Property Setup**

**Actors:** Landlord, Officer

**Flow:**
1. Landlord registers account → Status: `PENDING_PROFILE`
2. Landlord completes profile (ID, TIN, address, etc.) → Status: `PENDING_VERIFICATION`
3. Officer reviews and verifies profile → Status: `VERIFIED`
4. Landlord adds property → Property Status: `PENDING_OFFICER_REVIEW`
5. Officer verifies property → Property Status: `ACTIVE`
6. Landlord adds rental units to property
7. System ready for contract creation

**Notifications:**
- Officer notified when profile submitted
- Landlord notified when profile verified/rejected
- Officer notified when property submitted

---

### **Use Case 2: Tenant Registration**

**Actors:** Tenant, Officer

**Flow:**
1. Tenant registers account → Status: `PENDING_PROFILE`
2. Tenant completes profile (ID, TIN, address, etc.) → Status: `PENDING_VERIFICATION`
3. Officer reviews and verifies profile → Status: `VERIFIED`
4. Tenant can now accept contracts

**Notifications:**
- Officer notified when profile submitted
- Tenant notified when profile verified/rejected

---

### **Use Case 3: Contract Creation & Approval**

**Actors:** Landlord, Tenant, Officer

**Flow:**
1. Landlord creates contract draft → Status: `DRAFT`
2. Landlord submits contract → Status: `PENDING_CONFIRMATION`
3. Tenant receives notification
4. Tenant reviews and confirms contract → Status: `PENDING_OFFICER_REVIEW`
5. Officer receives notification
6. Officer reviews contract details
7. Officer approves contract → Status: `ACTIVE`

**Alternative Flow (Rejection):**
- Tenant rejects → Status: `REJECTED` (landlord notified)
- Officer rejects → Status: `REJECTED` (both parties notified)

**Notifications:**
- Tenant notified when contract submitted
- Landlord notified when tenant confirms
- Officer notified when tenant confirms
- Both parties notified when officer approves/rejects

---

### **Use Case 4: Rent Declaration & Tax Calculation**

**Actors:** Landlord, System, Officer

**Flow:**
1. Landlord navigates to active contract
2. Landlord submits rent declaration for period
3. System calculates:
   - Annual gross rent (monthly × 12)
   - Applies 40% deduction (if individual + residential)
   - Determines tax band
   - Calculates tax amount
   - Compares with AI benchmark
   - Flags anomalies if deviation > threshold
4. Declaration saved with tax details
5. If anomaly detected → Officer notified for review
6. Officer can verify declaration

**Tax Calculation Example:**
- Monthly Rent: 5,000 ETB
- Entity: Individual
- Property: House
- Deduction: Yes (40%)
- Annual Gross: 60,000 ETB
- After Deduction: 36,000 ETB
- Tax Band: 24,001-48,000 (10% rate, 2,400 deductible)
- Annual Tax: (36,000 × 0.10) - 2,400 = 1,200 ETB
- Monthly Tax: 100 ETB

---

### **Use Case 5: Anomaly Detection & Review**

**Actors:** System, Officer, Landlord

**Flow:**
1. System compares declared rent with AI benchmark
2. If deviation > threshold → Flag as anomaly
3. Officer sees anomaly in dashboard
4. Officer reviews:
   - Declared rent
   - AI benchmark rent
   - Property details
   - Historical declarations
5. Officer verifies or requests correction

**Example:**
- AI Benchmark: 10,000 ETB
- Threshold: 20%
- Declared: 7,000 ETB
- Deviation: 30% (flagged!)
- Reason: Possible under-declaration

---

### **Use Case 6: Appeal Process**

**Actors:** Tenant, Officer

**Flow:**
1. Tenant disagrees with rent/contract terms
2. Tenant submits appeal with:
   - Appeal type (RENT_OVERCHARGE, CONTRACT_VIOLATION, etc.)
   - Reason
   - Evidence documents
3. Officer receives notification
4. Officer reviews appeal and evidence
5. Officer makes decision:
   - Approve appeal → Contract adjusted/terminated
   - Reject appeal → Status quo maintained
6. Tenant notified of decision

**Appeal Types:**
- RENT_OVERCHARGE
- CONTRACT_VIOLATION
- PROPERTY_CONDITION
- EVICTION_DISPUTE
- OTHER

---

### **Use Case 7: System Configuration Update**

**Actors:** Administrator

**Flow:**
1. Admin logs into admin dashboard
2. Admin navigates to "System Settings"
3. Admin updates parameters:
   - Tax rate
   - Anomaly threshold
   - Rent increase cap
4. Admin saves configuration
5. Changes take effect immediately for new records
6. Existing records remain unchanged

**Impact:**
- New declarations use new tax rate
- New anomaly checks use new threshold
- New contracts enforce new rent cap

---

### **Use Case 8: Officer Management**

**Actors:** Administrator

**Flow:**
1. Admin navigates to "Manage Officers"
2. Admin clicks "Register Officer"
3. Admin enters officer details:
   - Name, email, phone
   - Assigned sub-city
   - Initial password
4. Officer account created → Status: `VERIFIED` (ready to use)
5. Officer can log in immediately
6. Admin can activate/deactivate officers as needed

---

### **Use Case 9: Property Verification**

**Actors:** Officer

**Flow:**
1. Officer logs in
2. Officer sees "Properties to Verify" count on dashboard
3. Officer navigates to "Properties" page
4. Officer reviews property details:
   - Property name, address, location
   - Owner information
   - Title deed document
   - Property type
5. Officer verifies property → Status: `ACTIVE`
6. Landlord can now add units

**Alternative:** Officer can reject property with reason

---

### **Use Case 10: Declaration Verification**

**Actors:** Officer

**Flow:**
1. Officer sees "Unverified Declarations" on dashboard
2. Officer navigates to "Declarations" page
3. Officer reviews declaration:
   - Declared rent
   - AI benchmark
   - Anomaly status
   - Tax calculation
4. Officer adds verification notes
5. Officer verifies declaration
6. Declaration marked as verified

---

## User Roles & Permissions

### **LANDLORD**
- ✅ Register and manage profile
- ✅ Add and manage properties
- ✅ Add rental units
- ✅ Create and submit contracts
- ✅ Submit rent declarations
- ✅ View tax calculations
- ❌ Cannot verify properties
- ❌ Cannot approve contracts

### **TENANT**
- ✅ Register and manage profile
- ✅ View assigned contracts
- ✅ Confirm or reject contracts
- ✅ Submit appeals
- ❌ Cannot create contracts
- ❌ Cannot add properties

### **SUBCITY_STAFF (Officer)**
- ✅ Verify landlord/tenant profiles
- ✅ Verify properties
- ✅ Approve/reject contracts
- ✅ Review declarations
- ✅ Review and decide appeals
- ✅ View sub-city analytics
- ❌ Cannot create contracts
- ❌ Cannot configure system settings

### **ADMINISTRATOR**
- ✅ Configure system parameters
- ✅ Register and manage officers
- ✅ View global metrics
- ✅ Access all system functions
- ✅ Override any decision

---

## Data Flow Summary

```
Registration → Profile Completion → Officer Verification → VERIFIED
    ↓
Property Registration → Officer Verification → ACTIVE
    ↓
Unit Addition (only for ACTIVE properties)
    ↓
Contract Creation (DRAFT) → Landlord Submit → PENDING_CONFIRMATION
    ↓
Tenant Confirm → PENDING_OFFICER_REVIEW
    ↓
Officer Approve → ACTIVE
    ↓
Rent Declaration → Tax Calculation → Anomaly Detection
    ↓
Officer Review (if anomaly) → Verification
```

---

## Key Business Rules

1. **Only VERIFIED users** can create contracts or properties
2. **Only ACTIVE properties** can have units added
3. **Only ACTIVE contracts** can have rent declarations
4. **40% deduction** only for residential properties (HOUSE, APARTMENT, MIXED_USE)
5. **Business entities** pay flat 30% tax (no deductions)
6. **Anomalies** are automatically flagged based on admin threshold
7. **Rent increases** are capped at admin-configured percentage
8. **Officers** can only see data from their assigned sub-city
9. **Administrators** have system-wide access

---

## Future Enhancements

1. **Tax Rule Versioning:** Support multiple tax rule versions
2. **Custom Tax Bands:** Admin-configurable tax brackets
3. **Analytics Dashboard:** City-wide metrics and trends
4. **Bulk Operations:** Bulk property/contract imports
5. **Reporting:** Generate tax reports, compliance reports
6. **Audit Trail:** Complete history of all changes
7. **Email Notifications:** In addition to in-app notifications
8. **Mobile App:** Mobile access for all users
