# Test Contract Creation - Email-Based Linking

## Changes Made

### Backend (2 files)
1. ✅ `ContractRequest.java` - Changed `tenantId` (UUID) → `tenantEmail` (String)
2. ✅ `RentalContractServiceImpl.java` - Added email lookup logic

### Frontend (1 file)
3. ✅ `CreateContract.jsx` - Send `tenantEmail` instead of `tenantId`

---

## Test Steps

### Step 1: Register Tenant User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tenant@test.com",
    "password": "password123",
    "firstName": "Alice",
    "lastName": "Smith",
    "phoneNumber": "+251922222222",
    "role": "TENANT"
  }'
```

**Expected**: 200 OK with user details

---

### Step 2: Login as Landlord
- Email: `test@example.com`
- Password: `password123`

---

### Step 3: Create Contract (Frontend)
1. Go to: http://localhost:5173/landlord/contracts
2. Click "Create Contract"
3. Fill form:
   - **Unit**: Select any unit
   - **Tenant Email**: `tenant@test.com` ✅ (registered tenant)
   - **Start Date**: 2024-06-01
   - **End Date**: 2024-12-31
   - **Monthly Rent**: 25000
4. Click "Save as Draft"

**Expected**: ✅ Success - "Contract saved as draft"

---

### Step 4: Test Error Case (Unregistered Tenant)
1. Create another contract
2. Use email: `notregistered@test.com` ❌ (not in system)
3. Click "Save as Draft"

**Expected**: ❌ Error - "Tenant with email 'notregistered@test.com' not found. Please ask the tenant to register first with this email address."

---

### Step 5: Login as Tenant
- Email: `tenant@test.com`
- Password: `password123`

---

### Step 6: View Contract (Tenant)
1. Go to: http://localhost:5173/tenant/contracts
2. Should see the contract created by landlord

**Expected**: ✅ Contract appears in tenant's dashboard

---

## API Test (Alternative)

### Create Contract via API
```bash
# Get landlord token first
TOKEN="your_landlord_jwt_token"

curl -X POST http://localhost:8080/api/contracts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "unitId": "your-unit-id",
    "tenantEmail": "tenant@test.com",
    "startDate": "2024-06-01",
    "endDate": "2024-12-31",
    "monthlyRent": 25000.00,
    "termsAndConditions": "Standard rental terms"
  }'
```

**Expected**: 201 Created with contract details

---

## What Changed

### Before (Broken)
```json
{
  "unitId": "uuid",
  "tenantId": "uuid",  // ❌ Landlord doesn't know this
  "startDate": "2024-06-01",
  "monthlyRent": 25000
}
```

### After (Fixed)
```json
{
  "unitId": "uuid",
  "tenantEmail": "tenant@test.com",  // ✅ Landlord knows this
  "startDate": "2024-06-01",
  "monthlyRent": 25000
}
```

---

## Success Criteria

- ✅ Contract creation works with tenant email
- ✅ Backend finds tenant by email
- ✅ Error message helpful when tenant not found
- ✅ Tenant sees contract after creation
- ✅ No changes to tenant registration flow
