# Tenant Contract Confirmation Issue - Diagnosis & Solution

## Problem Summary
Tenants cannot confirm contracts due to two interconnected issues:

### Issue 1: Database Constraint Violation (Immediate Blocker)
**Error:** `rental_contracts_status_check` constraint violation  
**Cause:** The code tries to set contract status to `PENDING_OFFICER_REVIEW`, but the database CHECK constraint doesn't allow this value.

**Database allows:**
- DRAFT
- PENDING_CONFIRMATION
- CONFIRMED ✅ (This should be used instead!)
- ACTIVE
- UNDER_APPEAL
- UNDER_REVIEW
- REJECTED
- TERMINATED
- EXPIRED

**Code enum has:**
- PENDING_OFFICER_REVIEW ❌ (Not in database!)

### Issue 2: Tenant Profile Verification Workflow
**Root Cause:** Tenants' accounts are not verified by officers, so they can't confirm contracts.

**Current Flow:**
1. Tenant registers → Status: `PENDING_PROFILE`
2. Tenant completes profile → Status changes to `PENDING_VERIFICATION` (if profile is complete)
3. Officer verifies profile → Status: `VERIFIED`
4. Only VERIFIED tenants can confirm contracts

**Profile Completion Requirements:**
- Date of Birth
- Residential Address
- National ID Number
- TIN Number
- Entity Type (INDIVIDUAL or BUSINESS)
- If BUSINESS: Business Registration Number

## Solutions

### Solution 1: Fix Database Constraint (Quick Fix)
Add `PENDING_OFFICER_REVIEW` to the database CHECK constraint:

```sql
-- Drop the old constraint
ALTER TABLE rental_contracts DROP CONSTRAINT rental_contracts_status_check;

-- Add new constraint with PENDING_OFFICER_REVIEW
ALTER TABLE rental_contracts ADD CONSTRAINT rental_contracts_status_check 
CHECK (status IN (
    'DRAFT',
    'PENDING_CONFIRMATION',
    'CONFIRMED',
    'PENDING_OFFICER_REVIEW',
    'ACTIVE',
    'UNDER_APPEAL',
    'UNDER_REVIEW',
    'REJECTED',
    'TERMINATED',
    'EXPIRED'
));
```

### Solution 2: Use Existing 'CONFIRMED' Status (Recommended)
Change the code to use `CONFIRMED` instead of `PENDING_OFFICER_REVIEW`:

**In RentalContractServiceImpl.java:**
```java
// Change from:
contract.setStatus(ContractStatus.PENDING_OFFICER_REVIEW);

// To:
contract.setStatus(ContractStatus.CONFIRMED);
```

**Then update the officer approval flow to check for CONFIRMED status:**
```java
if (contract.getStatus() != ContractStatus.CONFIRMED) {
    throw new RuntimeException("Contract is not confirmed by tenant");
}
```

### Solution 3: Tenant Profile Verification
**For Tenants to complete their profile:**

1. **Frontend:** Ensure Profile page shows all required fields:
   - Date of Birth
   - Residential Address
   - National ID Number
   - National ID Document Upload
   - TIN Number
   - Entity Type selector

2. **Backend:** Already implemented - when profile is complete, status automatically changes to `PENDING_VERIFICATION`

3. **Officer Dashboard:** Already has ProfileVerification page at `/officer/profile-verification`

## Recommended Action Plan

### Step 1: Fix the Contract Status Issue (Choose one)
**Option A (Quick):** Add `PENDING_OFFICER_REVIEW` to database constraint  
**Option B (Clean):** Use existing `CONFIRMED` status in code

### Step 2: Verify Tenant Profile Workflow
1. Check if tenant profile page has all required fields
2. Test: Complete tenant profile → Should change to PENDING_VERIFICATION
3. Officer should see pending profile in ProfileVerification page
4. Officer verifies → Tenant status becomes VERIFIED
5. Tenant can now confirm contracts

### Step 3: Test End-to-End
1. Register as tenant
2. Complete profile with all required fields
3. Officer verifies profile
4. Landlord creates contract
5. Tenant confirms contract
6. Officer approves contract
7. Contract becomes ACTIVE

## Current Status Check

**To check tenant account status:**
```sql
SELECT id, email, first_name, last_name, role, account_status, 
       date_of_birth, residential_address, national_id_number, tin_number, entity_type
FROM users 
WHERE role = 'TENANT'
ORDER BY created_at DESC;
```

**To check contract status:**
```sql
SELECT id, property_address, status, tenant_confirmed_at, officer_reviewed_at
FROM rental_contracts
ORDER BY created_at DESC;
```
