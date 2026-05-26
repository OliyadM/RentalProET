-- Migration: Add Ethiopian rental contract required fields
-- Phase 1: Critical fields for legal compliance

-- Financial Terms
ALTER TABLE rental_contracts ADD COLUMN payment_due_day INTEGER DEFAULT 1;
ALTER TABLE rental_contracts ADD COLUMN payment_method VARCHAR(50) DEFAULT 'BANK_TRANSFER';
ALTER TABLE rental_contracts ADD COLUMN security_deposit_amount DOUBLE PRECISION;

-- Contract Terms
ALTER TABLE rental_contracts ADD COLUMN notice_period_days INTEGER DEFAULT 30;
ALTER TABLE rental_contracts ADD COLUMN renewal_type VARCHAR(50) DEFAULT 'RENEGOTIATE';

-- Add constraints
ALTER TABLE rental_contracts ADD CONSTRAINT payment_due_day_check 
  CHECK (payment_due_day >= 1 AND payment_due_day <= 31);
  
ALTER TABLE rental_contracts ADD CONSTRAINT payment_method_check 
  CHECK (payment_method IN ('BANK_TRANSFER', 'CASH', 'MOBILE_MONEY', 'CHECK'));
  
ALTER TABLE rental_contracts ADD CONSTRAINT renewal_type_check 
  CHECK (renewal_type IN ('AUTO_RENEW', 'RENEGOTIATE', 'FIXED_TERM'));

-- Verification query
-- SELECT id, monthly_rent, security_deposit_amount, payment_due_day, payment_method, notice_period_days, renewal_type 
-- FROM rental_contracts ORDER BY created_at DESC LIMIT 5;
