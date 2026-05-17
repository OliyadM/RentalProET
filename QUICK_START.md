oard!**

---

## 📚 Next Steps After Testing

1. Test all user flows (Landlord, Tenant, Officer)
2. Add loading states to UI
3. Add error toast notifications
4. Add form validation
5. Implement file upload for documents
6. Add GIS heatmap visualization
7. Add real-time notifications

---

**Everything is integrated and ready! Start testing! 🚀**
------|--------|
| Authentication | ✅ | ✅ | Ready to test |
| Properties | ✅ | ✅ | Ready to test |
| Rental Units | ✅ | ✅ | Ready to test |
| Contracts | ✅ | ✅ | Ready to test |
| Declarations | ✅ | ✅ | Ready to test |
| Appeals | ✅ | ✅ | Ready to test |
| Analytics | ✅ | ✅ | Ready to test |

---

## 🎉 Ready to Test!

**Open your browser and go to:**
```
http://localhost:5173
```

**Try logging in with:**
- Email: `test@example.com`
- Password: `password123`

**You should be redirected to the Landlord Dashbgured for `http://localhost:5173`
2. Make sure frontend is running on port 5173
3. Check `application.yml` CORS settings

---

## 📝 Test Credentials

### Landlord
- Email: `test@example.com`
- Password: `password123`
- Role: `LANDLORD`

### Create More Users
Use the register endpoint or frontend to create:
- Tenant users (role: `TENANT`)
- Officer users (role: `SUBCITY_STAFF`)
- Admin users (role: `ADMINISTRATOR`)

---

## ✨ Integration Status

| Feature | Backend | Frontend | Status |
|---------|---------|----
docker exec -it rentalpro-postgres psql -U postgres -d rentalpro_db
```

---

## 🔧 Troubleshooting

### Backend won't start
1. Check PostgreSQL is running: `docker ps`
2. Check port 8080 is free: `Test-NetConnection localhost -Port 8080`
3. Check logs in terminal

### Frontend can't connect to backend
1. Check backend is running on port 8080
2. Check `.env.local` has correct `VITE_API_BASE_URL`
3. Check browser console for CORS errors
4. Restart frontend: `npm run dev`

### CORS errors
1. Backend CORS is confi /api/appeals/{id}/resolve` - Resolve appeal (Officer)
- ✅ `POST /api/appeals/{id}/reject` - Reject appeal (Officer)
- ✅ `GET /api/appeals/{id}` - Get appeal by ID

### Analytics
- ✅ `GET /api/analytics/benchmark/{propertyId}` - Get AI rent benchmark

---

## 🐳 Docker Commands

### Start PostgreSQL
```bash
cd Backend
docker-compose up -d
```

### Stop PostgreSQL
```bash
cd Backend
docker-compose down
```

### View PostgreSQL logs
```bash
docker logs rentalpro-postgres
```

### Access PostgreSQL CLI
```bashT /api/declarations/contract/{contractId}` - Get declarations
- ✅ `POST /api/declarations/contract/{contractId}` - Create declaration
- ✅ `GET /api/declarations/unverified` - Get unverified (Officer)
- ✅ `PUT /api/declarations/{id}/verify` - Verify declaration (Officer)
- ✅ `GET /api/declarations/anomalies/{subCity}` - Get anomalies

### Appeals
- ✅ `GET /api/appeals/my-appeals` - Get tenant's appeals
- ✅ `GET /api/appeals/pending` - Get pending appeals (Officer)
- ✅ `POST /api/appeals` - Create appeal
- ✅ `POSTnit
- ✅ `GET /api/units/{id}` - Get unit by ID

### Contracts
- ✅ `GET /api/contracts/my-contracts/landlord` - Get landlord's contracts
- ✅ `GET /api/contracts/my-contracts/tenant` - Get tenant's contracts
- ✅ `POST /api/contracts` - Create contract
- ✅ `PUT /api/contracts/{id}/submit` - Submit contract
- ✅ `POST /api/contracts/{id}/confirm` - Confirm contract (Tenant)
- ✅ `POST /api/contracts/{id}/reject` - Reject contract (Tenant)
- ✅ `GET /api/contracts/{id}` - Get contract by ID

### Declarations
- ✅ `GEister new user
- ✅ `POST /api/auth/login` - Login user

### Properties
- ✅ `GET /api/properties/my-properties` - Get landlord's properties
- ✅ `POST /api/properties` - Create property
- ✅ `GET /api/properties/{id}` - Get property by ID
- ✅ `PUT /api/properties/{id}/verify` - Verify property (Officer)
- ✅ `GET /api/properties/subcity/{subCity}` - Get properties by sub-city

### Rental Units
- ✅ `GET /api/units/property/{propertyId}` - Get units by property
- ✅ `POST /api/units/property/{propertyId}` - Create uils
4. Submit
5. **Expected:** Property created and visible in list

### Test 4: Create Rental Unit
1. Go to property detail page
2. Click "Add Unit"
3. Fill in unit details
4. Submit
5. **Expected:** Unit created and linked to property

### Test 5: Create Contract
1. Go to "Contracts" → "Create Contract"
2. Select unit and tenant
3. Fill in contract details
4. Submit
5. **Expected:** Contract created in DRAFT status

---

## 🔍 API Endpoints Reference

### Authentication
- ✅ `POST /api/auth/register` - Reger credentials:
   - Email: `test@example.com`
   - Password: `password123`
3. Click "Login"
4. **Expected:** Redirect to landlord dashboard

### Test 2: Register Flow
1. Open: `http://localhost:5173/register`
2. Fill in form with new user details
3. Click "Register"
4. **Expected:** Redirect to dashboard with JWT token

### Test 3: Create Property
1. Login as landlord
2. Go to "Properties" → "Add Property"
3. Fill in property detaple.com",
  "firstName": "Test",
  "lastName": "User",
  "role": "LANDLORD"
}
```
✅ **Status: WORKING**

---

### 3. ✅ Protected Endpoint (My Properties)
```bash
GET http://localhost:8080/api/properties/my-properties
Authorization: Bearer {token}
```
**Response:**
```json
[]
```
✅ **Status: WORKING** (Empty because no properties created yet)

---

## 🎯 Next Steps - Frontend Integration Testing

### Test 1: Login Flow
1. Open: `http://localhost:5173/login`
2. Ent",
  "firstName": "Test",
  "lastName": "User",
  "role": "LANDLORD"
}
```
✅ **Status: WORKING**

---

### 2. ✅ Login Endpoint
```bash
POST http://localhost:8080/api/auth/login
```
**Request:**
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```
**Response:**
```json
{
  "token": "eyJhbGci...",
  "userId": "ad085bab-18be-4f91-ae28-0980715f00af",
  "email": "test@examhttp://localhost:8080/api/auth/register
```
**Request:**
```json
{
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User",
  "phoneNumber": "+251911234567",
  "role": "LANDLORD"
}
```
**Response:**
```json
{
  "token": "eyJhbGci...",
  "userId": "ad085bab-18be-4f91-ae28-0980715f00af",
  "email": "test@example.comurrent Status

### Backend
- ✅ Running on: `http://localhost:8080/api`
- ✅ PostgreSQL: Running in Docker (port 5432)
- ✅ Authentication: Working (JWT)
- ✅ All endpoints: Ready

### Frontend
- ✅ Running on: `http://localhost:5173`
- ✅ Axios configured
- ✅ API integration: Ready

---

## 🧪 API Testing Results

### 1. ✅ Register Endpoint
```bash
POST # 🚀 RentalPro ET - Quick Start Guide

## ✅ C