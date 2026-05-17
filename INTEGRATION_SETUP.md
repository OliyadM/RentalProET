# RentalPro ET - Integration Setup Guide

## ✅ What We've Done

### Backend Changes
1. ✅ Created `RentalUnitController.java` - Missing REST controller for rental units
2. ✅ Updated `SecurityConfig.java` - CORS now uses environment variables
3. ✅ Updated `application.yml` - Added CORS configuration section
4. ✅ Created `.env.example` - Template for environment variables

### Frontend Changes
1. ✅ Installed `axios` package
2. ✅ Created `.env.example` - Template for environment variables
3. ✅ Created `.env.local` - Local development configuration
4. ✅ Created `src/config/api.js` - Axios instance with interceptors
5. ✅ Updated `src/services/api.js` - Replaced ALL mock data with real API calls

---

## 🚀 How to Run the Integrated System

### Step 1: Start the Backend

```bash
cd Backend

# Make sure PostgreSQL is running and database exists
# Database: rentalpro_db
# User: postgres
# Password: Mikias@123 (or update in application.yml)

# Run the Spring Boot application
./mvnw spring-boot:run

# Or on Windows:
mvnw.cmd spring-boot:run
```

**Backend will start on:** `http://localhost:8080`
**API base path:** `http://localhost:8080/api`

### Step 2: Start the Frontend

```bash
cd Frontend

# Install dependencies (if not already done)
npm install

# Start the development server
npm run dev
```

**Frontend will start on:** `http://localhost:5173`

---

## 🧪 Testing the Integration

### Test 1: Authentication Flow

1. **Open browser:** `http://localhost:5173`
2. **Click "Register"** or go to `/register`
3. **Fill in the form:**
   - First Name: Test
   - Last Name: User
   - Email: test@example.com
   - Password: password123
   - Phone: +251911234567
   - Role: LANDLORD
4. **Submit** - Should redirect to dashboard with JWT token

### Test 2: Login Flow

1. **Go to:** `http://localhost:5173/login`
2. **Enter credentials:**
   - Email: test@example.com
   - Password: password123
3. **Submit** - Should redirect to role-based dashboard

### Test 3: Check Browser Console

Open browser DevTools (F12) and check Console:
- ✅ Should see: `🚀 API Request: POST /auth/login`
- ✅ Should see: `✅ API Response: /auth/login 200`
- ❌ Should NOT see any CORS errors
- ❌ Should NOT see 404 errors

### Test 4: Check Network Tab

In DevTools Network tab:
- ✅ Request URL should be: `http://localhost:8080/api/auth/login`
- ✅ Status should be: `200 OK`
- ✅ Response should contain: `{ token: "...", ... }`
- ✅ Subsequent requests should have: `Authorization: Bearer <token>`

---

## 🔍 Troubleshooting

### Problem: CORS Error

**Error:** `Access to XMLHttpRequest blocked by CORS policy`

**Solution:**
1. Check backend is running on port 8080
2. Check `application.yml` has correct CORS configuration
3. Restart backend after changes

### Problem: 401 Unauthorized

**Error:** All API calls return 401

**Solution:**
1. Check JWT token is in localStorage: `rentalpro_user`
2. Check token is being sent in headers: `Authorization: Bearer <token>`
3. Check token is valid (not expired)

### Problem: Connection Refused

**Error:** `ERR_CONNECTION_REFUSED`

**Solution:**
1. Make sure backend is running: `http://localhost:8080/api`
2. Check `.env.local` has correct `VITE_API_BASE_URL`
3. Restart frontend after changing `.env.local`

### Problem: 404 Not Found

**Error:** `404` on API endpoints

**Solution:**
1. Check API endpoint exists in backend controller
2. Check URL path is correct (should start with `/api`)
3. Check HTTP method matches (GET, POST, PUT, DELETE)

---

## 📝 API Endpoints Reference

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Properties
- `GET /api/properties/my-properties` - Get landlord's properties
- `POST /api/properties` - Create property
- `GET /api/properties/{id}` - Get property by ID
- `PUT /api/properties/{id}/verify` - Verify property (Officer)

### Rental Units
- `GET /api/units/property/{propertyId}` - Get units by property
- `POST /api/units/property/{propertyId}` - Create unit
- `GET /api/units/{id}` - Get unit by ID

### Contracts
- `GET /api/contracts/my-contracts/landlord` - Get landlord's contracts
- `GET /api/contracts/my-contracts/tenant` - Get tenant's contracts
- `POST /api/contracts` - Create contract
- `PUT /api/contracts/{id}/submit` - Submit contract
- `POST /api/contracts/{id}/confirm` - Confirm contract (Tenant)
- `POST /api/contracts/{id}/reject` - Reject contract (Tenant)

### Declarations
- `GET /api/declarations/contract/{contractId}` - Get declarations
- `POST /api/declarations/contract/{contractId}` - Create declaration
- `GET /api/declarations/unverified` - Get unverified (Officer)
- `PUT /api/declarations/{id}/verify` - Verify declaration (Officer)

### Appeals
- `GET /api/appeals/my-appeals` - Get tenant's appeals
- `GET /api/appeals/pending` - Get pending appeals (Officer)
- `POST /api/appeals` - Create appeal
- `POST /api/appeals/{id}/resolve` - Resolve appeal (Officer)
- `POST /api/appeals/{id}/reject` - Reject appeal (Officer)

### Analytics
- `GET /api/analytics/benchmark/{propertyId}` - Get AI rent benchmark

---

## 🎯 Next Steps

### Immediate Testing
1. ✅ Test authentication (register/login)
2. ✅ Test property creation
3. ✅ Test unit creation
4. ✅ Test contract flow

### Future Enhancements
- [ ] Add loading states to UI
- [ ] Add error toast notifications
- [ ] Add form validation
- [ ] Add file upload for documents
- [ ] Implement GIS heatmap
- [ ] Add real-time notifications

---

## 📚 Environment Variables Reference

### Backend (.env or application.yml)
```yaml
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
```

### Frontend (.env.local)
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_ENV=local
VITE_DEBUG=true
```

---

## ✨ Key Features of This Integration

1. **Environment-based Configuration** - No hardcoded URLs
2. **Automatic JWT Handling** - Token added to every request
3. **Global Error Handling** - 401 errors auto-logout
4. **Debug Logging** - See all API calls in console
5. **Type-safe API Layer** - Single source of truth
6. **Production Ready** - Easy to deploy with env variables

---

## 🎉 Success Indicators

You'll know the integration is working when:
- ✅ No CORS errors in console
- ✅ Login redirects to dashboard
- ✅ JWT token stored in localStorage
- ✅ API calls show 200 status codes
- ✅ Data loads from backend (not mock data)
- ✅ Authorization header present in requests

---

**Ready to test? Start both servers and try logging in!** 🚀
