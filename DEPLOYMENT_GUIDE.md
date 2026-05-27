# RentalPro ET - Deployment Guide

## Overview
- **Frontend**: Vercel (Free tier)
- **Backend**: Render (Free tier)
- **Database**: Render PostgreSQL (Free 90 days, then $7/month)

## Deployment Order
Deploy in this exact order to avoid CORS issues:

### 1. Deploy Backend to Render (First)

#### Option A: Using Render Blueprint (Recommended)
1. Push your code to GitHub
2. Go to [Render Dashboard](https://dashboard.render.com/)
3. Click "New" → "Blueprint"
4. Connect your GitHub repository
5. Select the repository containing `render.yaml`
6. Render will automatically:
   - Create PostgreSQL database
   - Create web service
   - Link them together
   - Generate JWT_SECRET
7. **Manually set these environment variables** in Render Dashboard:
   - `ADMIN_SEED_PASSWORD`: Your admin password (e.g., `Admin@123`)
   - `CORS_ALLOWED_ORIGINS`: Leave empty for now (will update after frontend deploy)

#### Option B: Manual Setup
1. Create PostgreSQL Database:
   - Go to Render Dashboard → "New" → "PostgreSQL"
   - Name: `rentalpro-db`
   - Database: `rentalpro`
   - User: `rentalpro`
   - Plan: Free
   - Copy the "Internal Database URL"

2. Create Web Service:
   - Go to Render Dashboard → "New" → "Web Service"
   - Connect your GitHub repository
   - Settings:
     - Name: `rentalpro-backend`
     - Runtime: `Java`
     - Root Directory: `Backend`
     - Build Command: `./mvnw clean package -DskipTests`
     - Start Command: `java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/rentalpro-backend-1.0.0.jar`
   - Environment Variables:
     - `SPRING_PROFILES_ACTIVE`: `prod`
     - `DATABASE_URL`: (paste Internal Database URL from step 1)
     - `JWT_SECRET`: (generate random 64-char string)
     - `ADMIN_SEED_EMAIL`: `admin@rentalpro.et`
     - `ADMIN_SEED_PASSWORD`: Your admin password
     - `ADMIN_SEED_PHONE`: `+251900000000`
     - `CLOUDINARY_ENABLED`: `false`
     - `CORS_ALLOWED_ORIGINS`: Leave empty for now

3. Deploy and wait for build to complete (5-10 minutes)

4. **Copy your backend URL**: `https://rentalpro-backend-xxxx.onrender.com`

---

### 2. Deploy Frontend to Vercel (Second)

1. Go to [Vercel Dashboard](https://vercel.com/dashboard)
2. Click "Add New" → "Project"
3. Import your GitHub repository
4. Configure:
   - Framework Preset: Vite
   - Root Directory: `Frontend`
   - Build Command: `npm run build`
   - Output Directory: `dist`
5. **Add Environment Variable**:
   - Key: `VITE_API_BASE_URL`
   - Value: `https://rentalpro-backend-xxxx.onrender.com/api` (use your backend URL from step 1)
6. Click "Deploy"
7. Wait for deployment (2-3 minutes)
8. **Copy your frontend URL**: `https://rentalpro-et.vercel.app`

---

### 3. Update Backend CORS (Third)

1. Go back to Render Dashboard
2. Open your `rentalpro-backend` service
3. Go to "Environment" tab
4. Update `CORS_ALLOWED_ORIGINS` variable:
   - Value: `https://rentalpro-et.vercel.app` (use your frontend URL from step 2)
5. Save changes (this will trigger automatic redeploy)

---

## Post-Deployment Setup

### 4. Run Database Migrations

Your backend will automatically create tables on first startup (using `ddl-auto: update`), but you need to seed data manually.

#### Connect to Render PostgreSQL:
```bash
# Get connection details from Render Dashboard → rentalpro-db → "Info" tab
# Use any PostgreSQL client (pgAdmin, DBeaver, psql)

# Or use Render's built-in shell:
# Render Dashboard → rentalpro-db → "Shell" tab
```

#### Run seed files in this order:
1. `Backend/docs/NOTIFICATION_TYPES_MIGRATION.sql` - Add notification types
2. `Backend/docs/ADD_ETHIOPIAN_CONTRACT_FIELDS_MIGRATION.sql` - Add Ethiopian contract fields
3. `Backend/docs/ADD_MINIMUM_CONTRACT_YEARS_MIGRATION.sql` - Add minimum contract years
4. `Backend/docs/ADD_PENDING_OFFICER_REVIEW_STATUS_MIGRATION.sql` - Add pending officer review status
5. `Backend/docs/SEED_BOLE_PROPERTIES_ONLY.sql` - Seed demo data (optional)

---

### 5. Test Your Deployment

1. Open your frontend URL: `https://rentalpro-et.vercel.app`
2. Try to login with admin account:
   - Email: `admin@rentalpro.et`
   - Password: (whatever you set in `ADMIN_SEED_PASSWORD`)
3. If login fails, check:
   - Backend logs in Render Dashboard
   - Browser console for CORS errors
   - Network tab for API call failures

---

## Troubleshooting

### Backend won't start
- Check Render logs: Dashboard → rentalpro-backend → "Logs" tab
- Common issues:
  - Database connection failed (check DATABASE_URL)
  - Missing JWT_SECRET
  - Build failed (check Java version is 17)

### Frontend can't connect to backend
- Check CORS_ALLOWED_ORIGINS is set correctly
- Check VITE_API_BASE_URL includes `/api` at the end
- Check backend is running (visit backend URL in browser)

### Database connection issues
- Render free tier databases sleep after 15 minutes of inactivity
- First request after sleep will be slow (10-15 seconds)
- Check DATABASE_URL format: `postgresql://user:password@host:port/database`

### CORS errors
- Make sure CORS_ALLOWED_ORIGINS matches your frontend URL exactly
- No trailing slash in URL
- Must include protocol (https://)

---

## Environment Variables Reference

### Backend (Render)
| Variable | Required | Example | Notes |
|----------|----------|---------|-------|
| SPRING_PROFILES_ACTIVE | Yes | `prod` | Use production profile |
| DATABASE_URL | Yes | `postgresql://...` | Auto-set by Render Blueprint |
| JWT_SECRET | Yes | (auto-generated) | 64+ character random string |
| CORS_ALLOWED_ORIGINS | Yes | `https://rentalpro-et.vercel.app` | Set after frontend deploy |
| ADMIN_SEED_EMAIL | Yes | `admin@rentalpro.et` | Admin account email |
| ADMIN_SEED_PASSWORD | Yes | `Admin@123` | Admin account password |
| ADMIN_SEED_PHONE | Yes | `+251900000000` | Admin phone number |
| CLOUDINARY_ENABLED | No | `false` | Disable file uploads for now |

### Frontend (Vercel)
| Variable | Required | Example | Notes |
|----------|----------|---------|-------|
| VITE_API_BASE_URL | Yes | `https://rentalpro-backend-xxxx.onrender.com/api` | Backend URL with `/api` |

---

## Cost Breakdown

### Free Tier Limits
- **Vercel**: Unlimited deployments, 100GB bandwidth/month
- **Render Web Service**: 750 hours/month (enough for 1 service running 24/7)
- **Render PostgreSQL**: Free for 90 days, then $7/month

### After 90 Days
- Total cost: $7/month (just the database)
- Backend and frontend remain free

---

## CI/CD (Optional)

Both Vercel and Render support automatic deployments:
- **Vercel**: Auto-deploys on push to `main` branch
- **Render**: Auto-deploys on push to `main` branch

To enable:
1. Connect GitHub repository to both platforms
2. Set default branch to `main`
3. Enable "Auto-Deploy" in settings

---

## Monitoring

### Backend Logs
- Render Dashboard → rentalpro-backend → "Logs" tab
- Real-time log streaming
- Filter by log level (INFO, WARN, ERROR)

### Frontend Logs
- Vercel Dashboard → Project → "Deployments" → Click deployment → "Functions" tab
- Browser console for client-side errors

### Database Metrics
- Render Dashboard → rentalpro-db → "Metrics" tab
- Connection count, query performance, storage usage

---

## Backup Strategy

### Database Backups
Render free tier does NOT include automatic backups. To backup:

```bash
# Manual backup using pg_dump
pg_dump -h <host> -U <user> -d <database> > backup.sql

# Restore from backup
psql -h <host> -U <user> -d <database> < backup.sql
```

Recommended: Set up weekly manual backups or upgrade to paid plan for automatic backups.

---

## Next Steps

1. ✅ Fix render.yaml (DONE)
2. 🔄 Deploy backend to Render
3. 🔄 Deploy frontend to Vercel
4. 🔄 Update CORS settings
5. 🔄 Run database migrations
6. 🔄 Test the application
7. 🔄 Set up monitoring alerts (optional)
8. 🔄 Configure custom domain (optional)

---

## Support

- Render Docs: https://render.com/docs
- Vercel Docs: https://vercel.com/docs
- Spring Boot Docs: https://spring.io/projects/spring-boot
