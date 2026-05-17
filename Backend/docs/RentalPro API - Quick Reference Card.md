Base URL
http://localhost:8080

 Auth

| Method | Endpoint         | Body                                                        |
| ------ | ---------------- | ----------------------------------------------------------- |
| `POST` | `/auth/register` | `{email, password, firstName, lastName, phoneNumber, role}` |
| `POST` | `/auth/login`    | `{email, password}`                                         |

 Properties (Landlord)

| Method | Endpoint                    | Action             |
| ------ | --------------------------- | ------------------ |
| `POST` | `/properties`               | Create property    |
| `GET`  | `/properties/my-properties` | List my properties |
| `GET`  | `/properties/{id}`          | Get property       |

 Units (Landlord)

| Method | Endpoint                       | Action      |
| ------ | ------------------------------ | ----------- |
| `POST` | `/units/property/{propertyId}` | Create unit |
| `GET`  | `/units/property/{propertyId}` | List units  |

Contracts Landlord

| Method | Endpoint                           | Action                  |
| ------ | ---------------------------------- | ----------------------- |
| `POST` | `/contracts`                       | Create draft            |
| `PUT`  | `/contracts/{id}/submit`           | Submit for confirmation |
| `GET`  | `/contracts/my-contracts/landlord` | My contracts            |


     
    
Contract  Tenant

| Method | Endpoint                         | Body          |
| ------ | -------------------------------- | ------------- |
| `POST` | `/contracts/{id}/confirm`        | `{signature}` |
| `POST` | `/contracts/{id}/reject`         | `{reason}`    |
| `GET`  | `/contracts/my-contracts/tenant` | -             |

Staff/Admin

| Method | Endpoint                               |
| ------ | -------------------------------------- |
| `GET`  | `/contracts/by-status/{status}`        |
| `POST` | `/contracts/{id}/terminate?reason=...` |

Statuses: DRAFT → PENDING_CONFIRMATION → CONFIRMED → ACTIVE → TERMINATED

Declarations (Landlord)

| Method | Endpoint                              | Params                   |
| ------ | ------------------------------------- | ------------------------ |
| `POST` | `/declarations/contract/{contractId}` | `period`, `declaredRent` |
| `GET`  | `/declarations/contract/{contractId}` | -                        |


Staff Only 

| Method | Endpoint                            |
| ------ | ----------------------------------- |
| `GET`  | `/declarations/anomalies/{subCity}` |

Appeals  
        Tenants

| Method | Endpoint              | Body                                                  |
| ------ | --------------------- | ----------------------------------------------------- |
| `POST` | `/appeals`            | `{contractId, appealType, reason, evidenceDocuments}` |
| `GET`  | `/appeals/my-appeals` | -                                                     |


 Staff

| Method | Endpoint                | Params              |
| ------ | ----------------------- | ------------------- |
| `GET`  | `/appeals/pending`      | -                   |
| `POST` | `/appeals/{id}/resolve` | `decision`, `notes` |

Headers

Content-Type: application/json
Authorization: Bearer {token} 

Quick Workflow

1. POST /auth/register (Landlord + Tenant)
2. POST /auth/login (Landlord)
3. POST /properties → save propertyId
4. POST /units/property/{propertyId} → save unitId
5. POST /contracts → save contractId
6. PUT /contracts/{contractId}/submit
7. POST /auth/login (Tenant)
8. POST /contracts/{contractId}/confirm
9. POST /declarations/contract/{contractId}?period=2024-06-01&declaredRent=25000

Role Access 

| Role       | Access                                              |
| ---------- | --------------------------------------------------- |
| `LANDLORD` | Properties, Units, Contracts (own), Declarations    |
| `TENANT`   | Confirm/Reject Contracts, Appeals                   |
| `STAFF`    | Review Appeals, View Anomalies, Terminate Contracts |
| `ADMIN`    | All endpoints                                       |
