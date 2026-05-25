# Cloudinary Quick Setup (5 Minutes)

## Current Status
✅ Backend code ready
✅ Frontend code ready  
✅ File upload works with manual URL entry (fallback)
❌ Cloudinary not configured yet

---

## Setup Steps

### 1. Create Cloudinary Account (3 min)
1. Go to: https://cloudinary.com/users/register/free
2. Sign up with your email (NO credit card needed)
3. Verify email and log in

### 2. Get Credentials (1 min)
On the dashboard, you'll see:
```
Cloud name: dxyz123abc
API Key: 123456789012345  
API Secret: abcdefghijklmnopqrstuvwxyz123
```
**Copy these 3 values!**

### 3. Configure Backend (1 min)

**Option A: Using application.yml (for local testing)**
Edit `Backend/src/main/resources/application.yml`:
```yaml
cloudinary:
  enabled: true  # Change from false to true
  cloud-name: dxyz123abc  # Your cloud name
  api-key: 123456789012345  # Your API key
  api-secret: abcdefghijklmnopqrstuvwxyz123  # Your API secret
```

**Option B: Using .env file (recommended)**
Create `Backend/.env` file:
```env
CLOUDINARY_ENABLED=true
CLOUDINARY_CLOUD_NAME=dxyz123abc
CLOUDINARY_API_KEY=123456789012345
CLOUDINARY_API_SECRET=abcdefghijklmnopqrstuvwxyz123
```

### 4. Restart Backend
```bash
cd Backend
# Stop current backend (Ctrl+C)
.\mvnw.cmd spring-boot:run
```

### 5. Test Upload
1. Go to Profile page
2. Click "Upload" button for National ID
3. Select a file
4. Should upload to Cloudinary and show success ✅

---

## What Happens Without Cloudinary?

**Before Setup:**
- Users see upload button
- Click upload → shows error
- Component automatically switches to text input
- Users can enter URLs manually

**After Setup:**
- Users can upload files directly
- Files stored in Cloudinary
- URLs automatically saved to database

---

## File Organization in Cloudinary

Files are organized in folders:
```
rentalpro/
├── kyc/
│   ├── national-id/
│   └── business-reg/
├── properties/
│   └── title-deeds/
└── contracts/
```

---

## Troubleshooting

**Error: "File upload service is not configured"**
- Make sure `CLOUDINARY_ENABLED=true`
- Check credentials are correct
- Restart backend

**Files not appearing in Cloudinary**
- Log in to Cloudinary dashboard
- Go to Media Library
- Check the folders (kyc, properties, contracts)

---

## For Production (Render/Railway)

Add environment variables in your hosting platform:
```
CLOUDINARY_ENABLED=true
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

---

## Free Tier Limits

✅ 25GB storage
✅ 25GB bandwidth/month
✅ 25,000 transformations/month

More than enough for development and small production!
