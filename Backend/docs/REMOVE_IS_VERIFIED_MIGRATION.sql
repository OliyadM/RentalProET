-- Migration: Remove is_verified column from properties table
-- This migration consolidates property verification status into the single 'status' field

-- Step 1: Update existing data - set all verified properties to ACTIVE status
UPDATE properties 
SET status = 'ACTIVE', updated_at = NOW()
WHERE is_verified = true AND status != 'ACTIVE';

-- Step 2: Ensure all unverified properties have correct status
UPDATE properties 
SET status = 'PENDING_OFFICER_REVIEW', updated_at = NOW()
WHERE is_verified = false AND status = 'ACTIVE';

-- Step 3: Drop the redundant is_verified column
ALTER TABLE properties DROP COLUMN IF EXISTS is_verified;

-- Verification query to check the migration
-- SELECT id, property_name, status, created_at, updated_at FROM properties ORDER BY created_at DESC LIMIT 10;
