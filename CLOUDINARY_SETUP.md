# Cloudinary Setup Guide

This guide will help you set up Cloudinary for file uploads (images and PDFs) in the RentalPro application.

## Why Cloudinary?

- ✅ **Free tier:** 25GB storage, 25GB bandwidth/month
- ✅ **No credit card required** for signup
- ✅ **Easy integration:** Simple API
- ✅ **CDN included:** Fast file delivery worldwide
- ✅ **Perfect for:** Profile documents, property title deeds, contract PDFs

---

## Step 1: Create Cloudinary Account

1. Go to [https://cloudinary.com/users/register/free](https://cloudinary.com/users/register/free)
2. Sign up with your email (no credit card needed)
3. Verify your email address
4. Log in to your Cloudinary dashboard

---

## Step 2: Get Your API Credentials

Once logged in:

1. Go to **Dashboard** (you'll see it immediately after login)
2. Look for the **Account Details** section
3. You'll see three important values:
   - **Cloud Name** (e.g., `dxyz123abc`)
   - **API Key** (e.g., `123456789012345`)
   - **API Secret** (e.g., `abcdefghijklmnopqrstuvwxyz123`)

---

## Step 3: Configure Backend

### Option A: Using Environment Variables (Recommended for Production)

1. Create a `.env` file in the `Backend` folder (if it doesn't exist)
2. Add your Cloudinary credentials:

```env
CLOUDINARY_CLOUD_NAME=your-cloud-name-here
CLOUDINARY_API_KEY=your-api-key-here
CLOUDINARY_API_SECRET=your-api-secret-here
```

3. Make sure `.env` is in your `.gitignore` (it should be already)

### Option B: Direct Configuration (For Local Testing Only)

Edit `Backend/src/main/resources/application.yml`:

```yaml
cloudinary:
  cloud-name: your-cloud-name-here
  api-key: your-api-key-here
  api-secret: your-api-secret-here
```

⚠️ **Warning:** Don't commit real credentials to Git! Use environment variables for production.

---

## Step 4: Restart Backend

After adding credentials:

```bash
cd Backend

# Stop the running backend (Ctrl+C)

# Clean and recompile
.\mvnw.cmd clean compile

# Start the backend
.\mvnw.cmd spring-boot:run
```

---

## Step 5: Test File Upload

1. Start the frontend: `cd Frontend && npm run dev`
2. Log in to the application
3. Go to **Profile** page
4. Try uploading a document (National ID or Business Registration)
5. You should see:
   - Upload progress
   - Success message with green checkmark
   - File stored in Cloudinary

---

## Verify Upload in Cloudinary

1. Go to your Cloudinary dashboard
2. Click on **Media Library** in the left sidebar
3. You should see your uploaded files organized in folders:
   - `kyc/national-id/` - National ID documents
   - `kyc/business-reg/` - Business registration documents
   - `properties/title-deeds/` - Property title deeds
   - `contracts/` - Contract documents

---

## File Upload Limits

The application enforces these limits:

- **Max file size:** 10MB per file
- **Allowed types:** Images (JPG, PNG, etc.) and PDF files
- **Free tier limit:** 25GB total storage

---

## Folder Structure

Files are automatically organized in Cloudinary:

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

### Error: "Invalid credentials"
- Double-check your Cloud Name, API Key, and API Secret
- Make sure there are no extra spaces in the values
- Restart the backend after updating credentials

### Error: "File upload failed"
- Check your internet connection
- Verify file size is under 10MB
- Make sure file type is image or PDF

### Files not appearing in Cloudinary
- Check the Media Library in your Cloudinary dashboard
- Look in the correct folder (e.g., `kyc/national-id/`)
- Refresh the page

### Backend won't start
- Make sure you've added the Cloudinary dependency to `pom.xml`
- Run `.\mvnw.cmd clean compile` to download dependencies
- Check backend logs for specific error messages

---

## Production Deployment

When deploying to Render/Railway:

1. Add environment variables in your hosting platform:
   - `CLOUDINARY_CLOUD_NAME`
   - `CLOUDINARY_API_KEY`
   - `CLOUDINARY_API_SECRET`

2. These will automatically override the default values in `application.yml`

---

## Security Notes

- ✅ Never commit `.env` file to Git
- ✅ Never hardcode credentials in code
- ✅ Use environment variables for production
- ✅ Cloudinary URLs are public but hard to guess (UUID-based)
- ✅ Only authenticated users can upload files (enforced by backend)

---

## Cost Monitoring

Free tier includes:
- 25GB storage
- 25GB bandwidth per month
- 25,000 transformations per month

To monitor usage:
1. Go to Cloudinary Dashboard
2. Check **Usage** section
3. You'll see storage and bandwidth consumption

---

## Questions?

If you encounter issues:
1. Check the backend logs for error messages
2. Verify your Cloudinary credentials are correct
3. Make sure the backend is running and accessible
4. Test with a small image file first (< 1MB)
