# RentalPro ET - Deployment Guide

## 🚀 Quick Deploy (Free Tier)

### Prerequisites
- GitHub account
- Vercel account (sign up with GitHub)
- Railway account (sign up with GitHub)

---

## Step 1: Push to GitHub ✅ (Already Done)

Your code is at: `https://github.com/OliyadM/RentalProET`

---

## Step 2: Deploy Backend to Railway

### 2.1 Create Railway Account
1. Go to: https://railway.app
2. Click "Login with GitHub"
3. Authorize Railway

### 2.2 Create New Project
1. Click "New Project"
2. Select "Deploy from GitHub repo"
3. Choose: `OliyadM/RentalProET`
4. Railway will detect the backend automatically

### 2.3 Add PostgreSQL Database
1. In your project, click "+ New"
2. Select "Database" → "PostgreSQL"
3. Railway will create database and set `DATABASE_URL` automatically

### 2.4 Configure Environment Variables
Click on your backend service → "Variables" tab → Add these:

```
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production-min-256-bits
CORS_ALLOWED_ORIGINS=https://your-frontend-url.vercel.app
PORT=8080
```

**Important:** 
- Generate strong JWT_SECRET: https://www.grc.com/passwords.htm
- You'll update CORS_ALLOWED_ORIGINS after deploying frontend

### 2.5 Set Root Directory
1. Click "Settings" tab
2. Under "Build", set:
   - **Root Directory**: `Backend`
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/*.jar`

### 2.6 Deploy
1. Click "Deploy"
2. Wait 3-5 minutes for build
3. Copy your backend URL (e.g., `https://rentalpro-backend.railway.app`)

---

## Step 3: Deploy Frontend to Vercel

### 3.1 Create Vercel Account
1. Go to: https://vercel.com
2. Click "Sign Up" → "Continue with GitHub"
3. Authorize Vercel

### 3.2 Import Project
1. Click "Add New..." → "Project"
2. Import `OliyadM/RentalProET`
3. Vercel will detect React/Vite automatically

### 3.3 Configure Project
**Root Directory**: `Frontend`

**Environment Variables**: Add this variable:
```
VITE_API_BASE_URL=https://your-backend-url.railway.app/api
```
(Use the Railway URL from Step 2.6)

### 3.4 Deploy
1. Click "Deploy"
2. Wait 2-3 minutes
3. Copy your frontend URL (e.g., `https://rentalpro-et.vercel.app`)

---

## Step 4: Update CORS in Backend

1. Go back to Railway
2. Open your backend service
3. Update environment variable:
```
CORS_ALLOWED_ORIGINS=https://rentalpro-et.vercel.app
```
(Use your actual Vercel URL)

4. Railway will auto-redeploy

---

## Step 5: Test Deployment

1. Open your Vercel URL
2. Try to register a new user
3. Check browser console for errors
4. If you see CORS errors, double-check Step 4

---

## 🔄 Auto-Deploy Setup (CI/CD)

### Already Configured! ✅

**Every time you push to GitHub:**
- Vercel automatically deploys frontend
- Railway automatically deploys backend

**To deploy changes:**
```bash
git add .
git commit -m "Your changes"
git push origin main
```

Both will deploy automatically in ~3 minutes!

---

## 📊 Monitoring

### Railway Dashboard
- View logs: Railway → Your Service → "Logs" tab
- Check metrics: CPU, Memory, Network
- View deployments: "Deployments" tab

### Vercel Dashboard
- View deployments: Vercel → Your Project → "Deployments"
- Check analytics: "Analytics" tab
- View logs: Click on deployment → "Logs"

---

## 🐛 Troubleshooting

### Backend won't start
1. Check Railway logs for errors
2. Verify DATABASE_URL is set (should be automatic)
3. Check JWT_SECRET is set
4. Verify Java version (should be 17)

### Frontend can't connect to backend
1. Check VITE_API_BASE_URL in Vercel
2. Verify CORS_ALLOWED_ORIGINS in Railway
3. Check browser console for CORS errors
4. Ensure backend URL ends with `/api`

### Database connection failed
1. Railway PostgreSQL should auto-configure
2. Check DATABASE_URL format: `postgresql://user:pass@host:port/db`
3. Verify backend can reach database (check logs)

---

## 💰 Free Tier Limits

### Railway
- 500 hours/month (enough for 24/7 if optimized)
- $5 credit/month
- PostgreSQL included

### Vercel
- Unlimited deployments
- 100GB bandwidth/month
- Automatic SSL

**Both are free for your academic project!**

---

## 🔐 Security Checklist

Before going live:
- [ ] Change JWT_SECRET to strong random value
- [ ] Update CORS to only allow your frontend domain
- [ ] Review application.yml for sensitive data
- [ ] Enable HTTPS only (Railway/Vercel do this automatically)
- [ ] Set up database backups (Railway has automatic backups)

---

## 📝 Environment Variables Summary

### Railway (Backend)
```
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=<generate-strong-secret>
CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app
DATABASE_URL=<auto-set-by-railway>
PORT=8080
```

### Vercel (Frontend)
```
VITE_API_BASE_URL=https://your-backend.railway.app/api
```

---

## 🎉 You're Done!

Your app is now:
- ✅ Deployed to production
- ✅ Auto-deploying on git push
- ✅ Running on free tier
- ✅ Using PostgreSQL database
- ✅ SSL enabled
- ✅ Globally distributed (Vercel CDN)

**Live URLs:**
- Frontend: `https://your-app.vercel.app`
- Backend: `https://your-app.railway.app`

Share these URLs with your team or professors!
