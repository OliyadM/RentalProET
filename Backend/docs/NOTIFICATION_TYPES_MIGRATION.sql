-- Notification Types Migration
-- Adds new notification types to the CHECK constraint
-- Run this in pgAdmin after pulling latest changes

-- Step 1: Drop the old constraint
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;

-- Step 2: Add new constraint with all notification types
ALTER TABLE notifications ADD CONSTRAINT notifications_type_check 
CHECK (type IN (
    -- Contract lifecycle
    'CONTRACT_CONFIRMED',
    'CONTRACT_REJECTED',
    'CONTRACT_SUBMITTED',
    
    -- Appeals
    'APPEAL_SUBMITTED',
    'APPEAL_RESOLVED',
    'APPEAL_REJECTED',
    
    -- Declarations
    'DECLARATION_ANOMALY',
    
    -- Properties
    'PROPERTY_PENDING_REVIEW',
    'PROPERTY_VERIFIED',
    
    -- Account lifecycle (NEW)
    'ACCOUNT_CREATED',
    'PROFILE_PENDING_REVIEW',
    'ACCOUNT_VERIFIED',
    'ACCOUNT_REJECTED',
    
    -- General
    'SYSTEM'
));

-- Verify the constraint was added
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conname = 'notifications_type_check';
