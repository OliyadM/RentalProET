# Profile Management API

## Overview
This document describes the profile management endpoints for the KYC/verification workflow.

## Workflow
1. User registers → `accountStatus = PENDING_PROFILE`
2. User completes profile → `accountStatus = PENDING_VERIFICATION`
3. Officer verifies → `accountStatus = VERIFIED` or `REJECTED`

## Endpoints

### 1. Update Profile
**POST** `/api/users/profile`

Updates the authenticated user's profile with KYC information.

**Authentication:** Required (any authenticated user)

**Request Body:**
```json
{
  "dateOfBirth": "1990-05-15",
  "residentialAddress": "123 Main St, Addis Ababa",
  "nationalIdNumber": "ET123456789",
  "nationalIdDocumentUrl": "https://example.com/docs/national-id.pdf",
  "tinNumber": "TIN987654321",
  "entityType": "INDIVIDUAL",
  "businessRegNumber": null,
  "businessRegDocumentUrl": null
}
```

**For Business Entity:**
```json
{
  "dateOfBirth": "1985-03-20",
  "residentialAddress": "456 Business Ave, Addis Ababa",
  "nationalIdNumber": "ET987654321",
  "nationalIdDocumentUrl": "https://example.com/docs/national-id.pdf",
  "tinNumber": "TIN123456789",
  "entityType": "BUSINESS",
  "businessRegNumber": "BRN2024001",
  "businessRegDocumentUrl": "https://example.com/docs/business-reg.pdf"
}
```

**Response:** `ProfileResponse` (see below)

---

### 2. Get My Profile
**GET** `/api/users/profile/me`

Retrieves the authenticated user's profile.

**Authentication:** Required (any authenticated user)

**Response:** `ProfileResponse` (see below)

---

### 3. Get User Profile (Officer/Admin)
**GET** `/api/users/profile/{userId}`

Retrieves a specific user's profile.

**Authentication:** Required (SUBCITY_STAFF or ADMINISTRATOR)

**Response:** `ProfileResponse` (see below)

---

### 4. Get Pending Profiles
**GET** `/api/users/profiles/pending`

Retrieves all profiles pending verification.

**Authentication:** Required (SUBCITY_STAFF or ADMINISTRATOR)

**Response:** Array of `ProfileResponse`

---

### 5. Verify Profile
**POST** `/api/users/profiles/verify`

Verifies or rejects a user's profile.

**Authentication:** Required (SUBCITY_STAFF or ADMINISTRATOR)

**Request Body (Approve):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "VERIFIED",
  "verificationNotes": "All documents verified successfully"
}
```

**Request Body (Reject):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "REJECTED",
  "verificationNotes": "National ID document is unclear",
  "rejectionReason": "Please upload a clearer copy of your national ID"
}
```

**Response:** `ProfileResponse` (see below)

---

## Response Schema

### ProfileResponse
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "landlord@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+251911234567",
  "role": "LANDLORD",
  "subCityZone": null,
  "accountStatus": "VERIFIED",
  "dateOfBirth": "1990-05-15",
  "residentialAddress": "123 Main St, Addis Ababa",
  "nationalIdNumber": "ET123456789",
  "nationalIdDocumentUrl": "https://example.com/docs/national-id.pdf",
  "tinNumber": "TIN987654321",
  "entityType": "INDIVIDUAL",
  "businessRegNumber": null,
  "businessRegDocumentUrl": null,
  "verificationNotes": "All documents verified successfully",
  "verifiedById": "660e8400-e29b-41d4-a716-446655440001",
  "verifiedByName": "Officer Name",
  "verifiedAt": "2024-05-20T10:30:00",
  "rejectionReason": null,
  "createdAt": "2024-05-15T08:00:00",
  "updatedAt": "2024-05-20T10:30:00"
}
```

## Enums

### AccountStatus
- `PENDING_PROFILE` - Just registered, needs to complete profile
- `PENDING_VERIFICATION` - Profile submitted, waiting for officer review
- `VERIFIED` - Officer approved, can use platform
- `REJECTED` - Officer rejected, cannot use platform

### EntityType
- `INDIVIDUAL` - Personal landlord
- `BUSINESS` - Company/business entity

## Validation Rules

1. **Date of Birth:** Must be in the past
2. **National ID:** Must be unique across all users
3. **TIN Number:** Must be unique across all users
4. **Business Fields:** Required only when `entityType = BUSINESS`
5. **Rejection Reason:** Required when rejecting a profile

## Testing with cURL

### Update Profile
```bash
curl -X POST http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dateOfBirth": "1990-05-15",
    "residentialAddress": "123 Main St, Addis Ababa",
    "nationalIdNumber": "ET123456789",
    "nationalIdDocumentUrl": "https://example.com/docs/national-id.pdf",
    "tinNumber": "TIN987654321",
    "entityType": "INDIVIDUAL"
  }'
```

### Get My Profile
```bash
curl -X GET http://localhost:8080/api/users/profile/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Pending Profiles (Officer)
```bash
curl -X GET http://localhost:8080/api/users/profiles/pending \
  -H "Authorization: Bearer OFFICER_JWT_TOKEN"
```

### Verify Profile (Officer)
```bash
curl -X POST http://localhost:8080/api/users/profiles/verify \
  -H "Authorization: Bearer OFFICER_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "VERIFIED",
    "verificationNotes": "All documents verified"
  }'
```
