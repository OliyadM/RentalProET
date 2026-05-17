---

## 📁 File 3: API_WORKFLOW.md (Step-by-Step Usage)

Create `docs/API_WORKFLOW.md`:

```markdown
# Complete API Workflow Guide

Step-by-step examples for all major features.

---

## Prerequisites

- Backend running at `http://localhost:8080`
- Postman or similar tool
- Test users created (see below)

---

## 1. Setup Test Users

### Register Landlord
```http
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "email": "landlord@test.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+251911111111",
  "role": "LANDLORD"
}
Register Tenant
http
Copy
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "email": "tenant@test.com",
  "password": "password123",
  "firstName": "Alice",
  "lastName": "Smith",
  "phoneNumber": "+251922222222",
  "role": "TENANT"
}
Register Staff
http
Copy
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "email": "staff@subcity.gov.et",
  "password": "password123",
  "firstName": "Officer",
  "lastName": "Belay",
  "phoneNumber": "+251933333333",
  "role": "SUBCITY_STAFF",
  "subCityZone": "Bole"
}
2. Complete Contract Workflow
Step 1: Login as Landlord
http
Copy
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "email": "landlord@test.com",
  "password": "password123"
}
Save the token from response!
Step 2: Create Property
http
Copy
POST http://localhost:8080/properties
Content-Type: application/json
Authorization: Bearer {landlord_token}

{
  "propertyName": "Sunset Apartments",
  "address": "Bole Road, Addis Ababa",
  "subCity": "Bole",
  "woreda": "Woreda 03",
  "propertyType": "APARTMENT",
  "latitude": 9.0108,
  "longitude": 38.7613,
  "totalArea": 500.0,
  "yearBuilt": 2015
}
Save the propertyId from response!
Step 3: Create Unit
http
Copy
POST http://localhost:8080/units/property/{propertyId}
Content-Type: application/json
Authorization: Bearer {landlord_token}

{
  "unitNumber": "A-101",
  "floorArea": 120.5,
  "floorLevel": 1,
  "numberOfRooms": 3,
  "hasParking": true,
  "hasElevator": false,
  "amenities": "{\"wifi\": true, \"gym\": false}"
}
Save the unitId from response!
Step 4: Create Contract Draft
http
Copy
POST http://localhost:8080/contracts
Content-Type: application/json
Authorization: Bearer {landlord_token}

{
  "unitId": "{unitId}",
  "tenantId": "{tenant_id_from_registration}",
  "startDate": "2024-06-01",
  "endDate": "2024-12-31",
  "monthlyRent": 25000.00,
  "termsAndConditions": "Standard rental terms apply."
}
Save the contractId from response!
Step 5: Submit Contract
http
Copy
PUT http://localhost:8080/contracts/{contractId}/submit
Authorization: Bearer {landlord_token}
Status changes: DRAFT → PENDING_CONFIRMATION
Step 6: Login as Tenant
http
Copy
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "email": "tenant@test.com",
  "password": "password123"
}
Save the tenant token!
Step 7: View Pending Contract
http
Copy
GET http://localhost:8080/contracts/my-contracts/tenant
Authorization: Bearer {tenant_token}
Step 8: Confirm Contract
http
Copy
POST http://localhost:8080/contracts/{contractId}/confirm
Content-Type: application/json
Authorization: Bearer {tenant_token}

{
  "signature": "Alice Smith Digital Signature XYZ123"
}
Status changes: PENDING_CONFIRMATION → CONFIRMED → ACTIVE
3. Monthly Rent Declaration
Create Declaration (Landlord)
http
Copy
POST http://localhost:8080/declarations/contract/{contractId}?period=2024-06-01&declaredRent=25000.00
Authorization: Bearer {landlord_token}
AI automatically:
Calculates benchmark rent
Detects anomalies
Flags if suspicious
View Declarations
http
Copy
GET http://localhost:8080/declarations/contract/{contractId}
Authorization: Bearer {landlord_token}
4. Appeal Workflow
Create Appeal (Tenant)
http
Copy
POST http://localhost:8080/appeals
Content-Type: application/json
Authorization: Bearer {tenant_token}

{
  "contractId": "{contractId}",
  "appealType": "RENT_INCREASE",
  "reason": "The proposed rent is 30% above market rate for similar units in Bole",
  "evidenceDocuments": "[\"https://example.com/market_report.pdf\", \"https://example.com/comparable_listings.pdf\"]"
}
Contract status changes: ACTIVE → UNDER_APPEAL
View My Appeals (Tenant)
http
Copy
GET http://localhost:8080/appeals/my-appeals
Authorization: Bearer {tenant_token}
View Pending Appeals (Staff)
http
Copy
GET http://localhost:8080/appeals/pending
Authorization: Bearer {staff_token}
Resolve Appeal (Staff)
http
Copy
POST http://localhost:8080/appeals/{appealId}/resolve?decision=APPROVED&notes=Rent increase justified based on property improvements
Authorization: Bearer {staff_token}
Contract status changes: UNDER_APPEAL → ACTIVE
5. Analytics & Reporting
Get AI Rent Benchmark (Landlord)
http
Copy
GET http://localhost:8080/analytics/benchmark/{propertyId}
Authorization: Bearer {landlord_token}
Response:
JSON
Copy
{
  "propertyId": "...",
  "currentRent": 25000,
  "aiBenchmarkRent": 22000,
  "marketAverage": 23500,
  "percentile": 85,
  "recommendation": "ABOVE_MARKET"
}
View Anomalies (Staff)
http
Copy
GET http://localhost:8080/declarations/anomalies/Bole
Authorization: Bearer {staff_token}
Common Status Codes
Table
Code	Meaning	Action
200	Success	Request completed
201	Created	Resource created successfully
400	Bad Request	Check request body/params
401	Unauthorized	Token missing or expired
403	Forbidden	Wrong role for this action
404	Not Found	Resource doesn't exist
409	Conflict	Business rule violated (e.g., duplicate)
Testing Checklist
[ ] Register 3 users (landlord, tenant, staff)
[ ] Login each user and save tokens
[ ] Landlord: Create property → unit → contract
[ ] Landlord: Submit contract
[ ] Tenant: Confirm contract
[ ] Landlord: Create rent declaration
[ ] Tenant: Create appeal
[ ] Staff: Resolve appeal
[ ] All: View appropriate data