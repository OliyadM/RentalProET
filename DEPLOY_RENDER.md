# Deploy to Render (Free Alternative)

## Why Render?
- ✅ Truly free tier (no credit card needed)
- ✅ PostgreSQL included
- ✅ Auto-deploy from GitHub
- ✅ No credit limits

---

## Step 1: Create Render Account

1. Go to: https://render.com
2. Click "Get Started"
3. Sign up with GitHub
4. Authorize Render

---

## Step 2: Deploy Backend + Database

### Option A: Using Blueprint (Easiest)

1. Go to: https://dashboard.render.com/blueprints
2. Click "New Blueprint Instance"
3. Connect your GitHub repo: `OliyadM/RentalProET`
4. Render will detect `render.yaml` automatically
5. Click "Apply"
6. Wait 5-10 minutes for deployment

### Option B: Manual Setup

#### 2.1 Create PostgreSQL Database
1. Dashboard → "New +" → "PostgreSQL"
2. Name: `rentalpro-db`
3. Database: `rentalpro`
4. User: `rentalpro`
5. Region: Choose closest to you
6. Plan: **Free**
7. Click "Create Database"
8. Copy the **Internal Database URL** (starts with `postgresql://`)

#### 2.2 Create Backend Service
1. Dashboard → "New +" → "Web Service"
2. Connect repository: `OliyadM/RentalProET`
3. Configure:
   - **Name**: `rentalpro-backend`
   - **Region**: Same as database
   - **Branch**: `main`
   - **Root Directory**: `Backend`
   - **Runtime**: `Java`
   - **Build Command**: 
     ```
     chmod +x mvnw && ./mvnw clean package -DskipTests
     ```
   - **Start Command**: 
     ```
     java -Dspring.profiles.active=prod -jar target/*.jar
     ```
   - **Plan**: **Free**

4. Add Environment Variables:
   ```
   SPRING_PROFILES_ACTIVE=prod
   JWT_SECRET=your-super-secret-jwt-key-min-256-bits-change-this
   DATABASE_URL=<paste-internal-database-url-from-step-2.1>
   CORS_ALLOWED_ORIGINS=https://your-frontend-url.vercel.app
   ```

5. Click "Create Web Service"
6. Wait 5-10 minutes for first deployment
7. Copy your backend URL (e.g., `https://rentalpro-backend.onrender.com`)

---

## Step 3: Deploy Frontend to Vercel

1. Go to: https://vercel.com
2. Sign in with GitHub
3. "Add New..." → "Project"
4. Import `OliyadM/RentalProET`
5. Configure:
   - **Root Directory**: `Frontend`
   - **Framework Preset**: Vite
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`

6. Add Environment Variable:
   ```
   VITE_API_BASE_URL=https://rentalpro-backend.onrender.com/api
   ```
   (Use your Render backend URL from Step 2)

7. Click "Deploy"
8. Wait 2-3 minutes
9. Copy your frontend URL (e.g., `https://rentalpro-et.vercel.app`)

---

## Step 4: Update CORS

1. Go back to Render dashboard
2. Open your backend service
3. Go to "Environment" tab
4. Update `CORS_ALLOWED_ORIGINS`:
   ```
   https://rentalpro-et.vercel.app
   ```
   (Use your actual Vercel URL)
5. Click "Save Changes"
6. Render will auto-redeploy

---

## Step 5: Test Your Deployment

1. Open your Vercel URL
2. Try to register a new user
3. Check browser console for errors
4. If CORS errors appear, verify Step 4

---

## 🔄 Auto-Deploy (CI/CD)

**Already configured!** Every `git push` triggers:
- ✅ Render rebuilds backend (5-10 min)
- ✅ Vercel rebuilds frontend (2-3 min)

```bash
git add .
git commit -m "Your changes"
git push origin main
# Both deploy automatically!
```

---

## ⚠️ Important Notes

### Render Free Tier Limitations:
- **Cold starts**: Service sleeps after 15 min of inactivity
- **First request**: Takes 30-60 seconds to wake up
- **Build time**: 10 minutes max
- **750 hours/month**: Enough for testing

### Solutions:
1. **Keep alive**: Use a service like UptimeRobot to ping every 14 minutes
2. **Upgrade**: $7/month removes cold starts
3. **Accept it**: Fine for academic projects

---

## 🐛 Troubleshooting

### Backend won't start
1. Check Render logs: Service → "Logs" tab
2. Verify DATABASE_URL is set correctly
3. Check JWT_SECRET is set
4. Ensure Java 17 is being used

### Frontend can't connect
1. Verify VITE_API_BASE_URL in Vercel
2. Check CORS_ALLOWED_ORIGINS in Render
3. Ensure backend URL ends with `/api`
4. Check browser console for errors

### Database connection failed
1. Use **Internal Database URL** (not External)
2. Format: `postgresql://user:pass@host:port/db`
3. Check database is running in Render dashboard

### Build timeout
1. Render free tier has 10-minute build limit
2. Our build takes ~5-7 minutes (should be fine)
3. If timeout, try: `./mvnw clean package -DskipTests -T 1C`

---

## 💰 Cost Comparison

| Service | Free Tier | Limitations |
|---------|-----------|-------------|
| **Render** | ✅ Free forever | Cold starts, 750 hrs/month |
| **Vercel** | ✅ Free forever | 100GB bandwidth/month |
| Railway | ❌ $5 credit/month | Runs out quickly |

**Total Cost: $0/month** ✅

---

## 📊 Monitoring

### Render Dashboard
- View deployment logs
- Check service status
- Monitor database connections
- See build history

### Vercel Dashboard
- View all deployments
- Check analytics
- See build logs
- Monitor performance

---

## 🎉 You're Done!

Your app is now:
- ✅ Deployed to production
- ✅ Auto-deploying on git push
- ✅ Running on 100% free tier
- ✅ Using PostgreSQL database
- ✅ SSL enabled
- ✅ Globally distributed

**Live URLs:**
- Frontend: `https://your-app.vercel.app`
- Backend: `https://your-app.onrender.com`

Share with your team! 🚀
