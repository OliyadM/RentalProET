-- Fix properties table schema drift
-- The 'is_verified' column was removed from the Property entity (replaced by 'status')
-- but was never dropped from the DB, causing NOT NULL constraint failures on every INSERT.
-- Run this in pgAdmin against rentalpro_db.

DO $$
BEGIN
    -- 1. Drop is_verified — it is no longer in the entity and has no default,
    --    so it blocks every property creation.
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'properties' AND column_name = 'is_verified'
    ) THEN
        ALTER TABLE properties DROP COLUMN is_verified;
        RAISE NOTICE 'Dropped column is_verified from properties.';
    ELSE
        RAISE NOTICE 'Column is_verified does not exist — skipping drop.';
    END IF;

    -- 2. Give status a DB-level default so existing rows without a status
    --    are not left in an invalid state on future schema updates.
    ALTER TABLE properties
        ALTER COLUMN status SET DEFAULT 'PENDING_OFFICER_REVIEW';
    RAISE NOTICE 'Set default PENDING_OFFICER_REVIEW on properties.status.';
END $$;
