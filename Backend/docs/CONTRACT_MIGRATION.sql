-- Contract Enhancement Migration
-- Run this in pgAdmin to add new fields to rental_contracts table

DO $$ 
BEGIN
    -- payment_frequency
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='rental_contracts' AND column_name='payment_frequency') THEN
        ALTER TABLE rental_contracts ADD COLUMN payment_frequency VARCHAR(50);
    END IF;
    
    -- contract_document_url
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='rental_contracts' AND column_name='contract_document_url') THEN
        ALTER TABLE rental_contracts ADD COLUMN contract_document_url VARCHAR(255);
    END IF;
    
    -- additional_clauses (replaces terms_and_conditions)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='rental_contracts' AND column_name='additional_clauses') THEN
        ALTER TABLE rental_contracts ADD COLUMN additional_clauses VARCHAR(2000);
    END IF;
    
    -- officer_reviewed_at
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='rental_contracts' AND column_name='officer_reviewed_at') THEN
        ALTER TABLE rental_contracts ADD COLUMN officer_reviewed_at TIMESTAMP;
    END IF;
    
    -- reviewed_by_id
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='rental_contracts' AND column_name='reviewed_by_id') THEN
        ALTER TABLE rental_contracts ADD COLUMN reviewed_by_id UUID;
        ALTER TABLE rental_contracts ADD CONSTRAINT fk_reviewed_by FOREIGN KEY (reviewed_by_id) REFERENCES users(id);
    END IF;
END $$;

-- Migrate data from terms_and_conditions to additional_clauses if needed
UPDATE rental_contracts 
SET additional_clauses = terms_and_conditions 
WHERE additional_clauses IS NULL AND terms_and_conditions IS NOT NULL;

-- Update existing CONFIRMED contracts to PENDING_OFFICER_REVIEW
UPDATE rental_contracts 
SET status = 'PENDING_OFFICER_REVIEW' 
WHERE status = 'CONFIRMED';
