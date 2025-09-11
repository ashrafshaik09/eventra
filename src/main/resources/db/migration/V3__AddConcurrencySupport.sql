-- V3__AddConcurrencySupport.sql
-- Add concurrency support fields and constraints for atomic booking operations
-- (Schema changes only - indexes in separate migration)

-- Add idempotency key to bookings table for duplicate prevention (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'bookings' AND column_name = 'idempotency_key') THEN
        ALTER TABLE bookings ADD COLUMN idempotency_key VARCHAR(255);
    END IF;
END $$;

-- Add unique constraint to prevent duplicate bookings per user per event (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE constraint_name = 'uk_user_event_booking' AND table_name = 'bookings') THEN
        ALTER TABLE bookings ADD CONSTRAINT uk_user_event_booking UNIQUE (user_id, event_id);
    END IF;
END $$;

-- Add unique constraint for idempotency key (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE constraint_name = 'uk_bookings_idempotency_key' AND table_name = 'bookings') THEN
        ALTER TABLE bookings ADD CONSTRAINT uk_bookings_idempotency_key UNIQUE (idempotency_key);
    END IF;
END $$;

-- Add database-level constraints for data integrity (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.check_constraints 
                   WHERE constraint_name = 'chk_available_seats_non_negative') THEN
        ALTER TABLE events ADD CONSTRAINT chk_available_seats_non_negative CHECK (available_seats >= 0);
    END IF;
END $$;

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.check_constraints 
                   WHERE constraint_name = 'chk_available_seats_le_capacity') THEN
        ALTER TABLE events ADD CONSTRAINT chk_available_seats_le_capacity CHECK (available_seats <= capacity);
    END IF;
END $$;

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.check_constraints 
                   WHERE constraint_name = 'chk_quantity_positive') THEN
        ALTER TABLE bookings ADD CONSTRAINT chk_quantity_positive CHECK (quantity > 0);
    END IF;
END $$;

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.check_constraints 
                   WHERE constraint_name = 'chk_status_valid') THEN
        ALTER TABLE bookings ADD CONSTRAINT chk_status_valid CHECK (status IN ('CONFIRMED', 'CANCELLED'));
    END IF;
END $$;

-- Add comments for documentation
COMMENT ON COLUMN bookings.idempotency_key IS 'Unique key to prevent duplicate bookings on retries';
COMMENT ON CONSTRAINT uk_user_event_booking ON bookings IS 'Prevents users from booking the same event multiple times';
