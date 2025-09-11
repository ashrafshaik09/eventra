-- V3__AddConcurrencySupport.sql
-- Add concurrency support fields and constraints for atomic booking operations

-- Add idempotency key to bookings table for duplicate prevention
ALTER TABLE bookings 
ADD COLUMN idempotency_key VARCHAR(255) UNIQUE;

-- Add unique constraint to prevent duplicate bookings per user per event
ALTER TABLE bookings 
ADD CONSTRAINT uk_user_event_booking UNIQUE (user_id, event_id);

-- Create indexes for concurrency-critical queries
CREATE INDEX CONCURRENTLY idx_bookings_idempotency_key ON bookings (idempotency_key) WHERE idempotency_key IS NOT NULL;
CREATE INDEX CONCURRENTLY idx_bookings_user_status ON bookings (user_id, status);
CREATE INDEX CONCURRENTLY idx_bookings_event_status ON bookings (event_id, status);
CREATE INDEX CONCURRENTLY idx_events_available_seats ON events (available_seats) WHERE available_seats > 0;

-- Add database-level constraints for data integrity
ALTER TABLE events 
ADD CONSTRAINT chk_available_seats_non_negative CHECK (available_seats >= 0),
ADD CONSTRAINT chk_available_seats_le_capacity CHECK (available_seats <= capacity);

ALTER TABLE bookings 
ADD CONSTRAINT chk_quantity_positive CHECK (quantity > 0),
ADD CONSTRAINT chk_status_valid CHECK (status IN ('CONFIRMED', 'CANCELLED'));

-- Create partial indexes for performance optimization
CREATE INDEX CONCURRENTLY idx_events_upcoming ON events (starts_at) WHERE starts_at > NOW();
CREATE INDEX CONCURRENTLY idx_bookings_confirmed ON bookings (event_id) WHERE status = 'CONFIRMED';

-- Add comments for documentation
COMMENT ON COLUMN bookings.idempotency_key IS 'Unique key to prevent duplicate bookings on retries';
COMMENT ON CONSTRAINT uk_user_event_booking ON bookings IS 'Prevents users from booking the same event multiple times';
COMMENT ON INDEX idx_events_available_seats IS 'Optimizes seat availability queries for concurrent booking operations';
