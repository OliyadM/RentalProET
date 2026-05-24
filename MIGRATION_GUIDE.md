# Database Migration Guide

## Overview
This guide helps you apply database changes after pulling the latest code. The backend uses `ddl-auto: update` which automatically creates new columns, but some data migrations need to be run manually.

## Required Migrations

### 1. Contract Enhancement Migration
**File:** `Backend/docs/CONTRACT_MIGRATION.sql`

**What it does:**
- Adds new contract fields: `payment_frequency`, `contract_document_url`, `additional_clauses`, `officer_reviewed_at`, `reviewed_by_id`
- Migrates data from old `terms_and_conditions` to new `additional_clauses` field
- Updates existing `CONFIRMED` contracts to `PENDING_OFFICER_REVIEW` status
- Adds foreign key constraint for officer review tracking

**When to run:** After pulling code with contract enhancements

---

## How to Run Migrations

### Option 1: Using pgAdmin (Recommended)
1. Open pgAdmin and connect to your database
2. Navigate to: `Servers` → `PostgreSQL` → `Databases` → `rentalpro_db`
3. Right-click on `rentalpro_db` → Select `Query Tool`
4. Copy the entire content from `Backend/docs/CONTRACT_MIGRATION.sql`
5. Paste into the Query Tool
6. Click the Execute button (▶️) or press F5
7. Check for success message

### Option 2: Using psql Command Line
```bash
# Navigate to Backend/docs folder
cd Backend/docs

# Run the migration
psql -h localhost -p 5433 -U postgres -d rentalpro_db -f CONTRACT_MIGRATION.sql

# Enter password when prompted: Mikias@123
```

---

## After Running Migrations

### 1. Restart the Backend
The backend needs to be restarted to pick up the new database schema:

```bash
# Navigate to Backend folder
cd Backend

# Stop the running backend (Ctrl+C in the terminal where it's running)

# Clean and recompile
.\mvnw.cmd clean compile

# Start the backend
.\mvnw.cmd spring-boot:run
```

### 2. Verify Migration Success
Check the backend logs for:
- No SQL errors on startup
- Successful connection to database
- No missing column errors

---

## Migration Safety Features

All migrations use `IF NOT EXISTS` checks, which means:
- ✅ Safe to run multiple times
- ✅ Won't fail if columns already exist
- ✅ Won't duplicate data

---

## Troubleshooting

### Error: "column already exists"
This is normal if you've already run the migration. The script will skip existing columns.

### Error: "relation does not exist"
Make sure you're connected to the correct database (`rentalpro_db`) and that the backend has run at least once to create the base tables.

### Error: "permission denied"
Make sure you're using the `postgres` user with password `Mikias@123`.

---

## Database Connection Details
- **Host:** localhost
- **Port:** 5433
- **Database:** rentalpro_db
- **Username:** postgres
- **Password:** Mikias@123

---

## Important Notes

1. **Always backup before migrations** (optional but recommended for production)
2. **Run migrations in order** if multiple files exist
3. **Restart backend after migrations** to ensure schema is in sync
4. **Test in development first** before applying to production
5. **Commit migrations with code changes** so team members know what to run

---

## Questions?
If you encounter issues:
1. Check the backend logs for specific error messages
2. Verify database connection details
3. Ensure PostgreSQL is running (check Docker: `docker ps`)
4. Make sure you're on the correct branch with latest code
