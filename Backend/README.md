
---

## ✨ Key Features

### For Landlords 👨‍💼
| Feature | Description |
|---------|-------------|
| Property Registration | Register properties with GIS coordinates |
| Unit Management | Manage individual rental units |
| Contract Creation | Digital contracts with tenant confirmation |
| Rent Declaration | Monthly rent reporting with AI validation |
| Tax Preview | Estimated tax calculations |

### For Tenants 🧑‍💼
| Feature | Description |
|---------|-------------|
| Contract Review | View and confirm contract terms |
| Digital Signature | Electronically sign agreements |
| Appeal System | Dispute unfair rent increases |
| Receipt Access | Download digital receipts |

### For Government Staff 🏛️
| Feature | Description |
|---------|-------------|
| Anomaly Detection | AI-flagged suspicious declarations |
| Appeal Resolution | Review and resolve tenant appeals |
| Market Analytics | GIS heatmaps and rent benchmarks |
| Audit Trail | Complete transaction history |

---

## 🛠️ Technology Stack

### Backend (This Repository)
| Component | Technology | Purpose |
|-----------|-----------|---------|
| Framework | Spring Boot 3.x | Core application |
| Security | Spring Security + JWT | Authentication & Authorization |
| Data Access | Spring Data JPA | Database operations |
| Database | PostgreSQL 14+ | Primary data storage |
| Spatial | PostGIS | Geographic data |
| Migration | Flyway | Database versioning |
| Build Tool | Maven | Dependency management |

### Frontend (Separate Repository)
- React.js / Vue.js
- Leaflet (Maps)
- Chart.js (Analytics)

---

## 🚀 Getting Started

### Prerequisites

- [ ] Java 17 or higher ([Download](https://adoptium.net/))
- [ ] Maven 3.8+ (included as wrapper: `./mvnw`)
- [ ] PostgreSQL 14+ ([Download](https://postgresql.org/download/))
- [ ] Git ([Download](https://git-scm.com/downloads))
- [ ] IDE (IntelliJ IDEA recommended)

### Installation Steps

#### 1. Clone Repository

```bash
git clone https://github.com/Mikiasbefekadu/rentalpro-backend.git
cd rentalpro-backend

Core API Endpoints

Contract Lifecycle 
| Step | Method | Endpoint                      | Auth Required | Description              |
| ---- | ------ | ----------------------------- | ------------- | ------------------------ |
| 1    | POST   | `/contracts`                  | LANDLORD      | Create draft contract    |
| 2    | PUT    | `/contracts/{id}/submit`      | LANDLORD      | Submit for tenant review |
| 3    | POST   | `/contracts/{id}/confirm`     | TENANT        | Tenant confirms & signs  |
| 4    | POST   | `/declarations/contract/{id}` | LANDLORD      | Declare monthly rent     |

Appeals
| Step | Method | Endpoint                      | Auth Required | Description              |
| ---- | ------ | ----------------------------- | ------------- | ------------------------ |
| 1    | POST   | `/contracts`                  | LANDLORD      | Create draft contract    |
| 2    | PUT    | `/contracts/{id}/submit`      | LANDLORD      | Submit for tenant review |
| 3    | POST   | `/contracts/{id}/confirm`     | TENANT        | Tenant confirms & signs  |
| 4    | POST   | `/declarations/contract/{id}` | LANDLORD      | Declare monthly rent     |

Environment variables included:
baseUrl: http://localhost:8080
accessToken: (auto-filled after login)
contractId, unitId, etc. (auto-filled during workflow)