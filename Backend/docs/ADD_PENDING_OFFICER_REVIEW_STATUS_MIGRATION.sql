-- Migration: Add PENDING_OFFICER_REVIEW to rental_contracts status constraint
-- This allows contracts to be in PENDING_OFFICER_REVIEW state after tenant confirmation

-- Step 1: Drop the old constraint
ALTER TABLE rental_contracts DROP CONSTRAINT IF EXISTS rental_contracts_status_check;

-- Step 2: Add new constraint with PENDING_OFFICER_REVIEW included
ALTER TABLE rental_contracts ADD CONSTRAINT rental_contracts_status_check 
CHECK (status IN (
    'DRAFT',
    'PENDING_CONFIRMATION',
    'CONFIRMED',
    'PENDING_OFFICER_REVIEW',  -- Added for tenant-confirmed contracts awaiting officer approval
    'ACTIVE',
    'UNDER_APPEAL',
    'UNDER_REVIEW',
    'REJECTED',
    'TERMINATED',
    'EXPIRED'
));

-- Verification query
-- SELECT DISTINCT status FROM rental_contracts ORDER BY status;
