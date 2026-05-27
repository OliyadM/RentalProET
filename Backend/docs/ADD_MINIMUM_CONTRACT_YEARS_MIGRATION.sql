-- Migration: Add minimum_contract_years to system_config
-- Run this in pgAdmin against your active database (rentalpro_db or rentalpro).
--
-- Safe to run multiple times — the IF NOT EXISTS guard makes it idempotent.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'system_config'
          AND column_name = 'minimum_contract_years'
    ) THEN
        -- Add the column with a default so existing rows are not left NULL,
        -- satisfying the NOT NULL constraint on the entity.
        ALTER TABLE system_config
            ADD COLUMN minimum_contract_years INTEGER NOT NULL DEFAULT 2;

        RAISE NOTICE 'Column minimum_contract_years added to system_config with default value 2.';
    ELSE
        RAISE NOTICE 'Column minimum_contract_years already exists — skipping.';
    END IF;
END $$;
