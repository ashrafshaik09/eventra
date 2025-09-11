-- V4__AddConcurrencyIndexes.sql
-- Add performance-optimized indexes for concurrent booking operations

-- Create indexes for concurrency-critical queries (idempotency key partial index)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_bookings_idempotency_key') THEN
        CREATE INDEX CONCURRENTLY idx_bookings_idempotency_key ON bookings (idempotency_key) WHERE idempotency_key IS NOT NULL;
    END IF;
END $$;

-- Index for user bookings queries
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_bookings_user_status') THEN
        CREATE INDEX CONCURRENTLY idx_bookings_user_status ON bookings (user_id, status);
    END IF;
END $$;

-- Index for event bookings queries
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_bookings_event_status') THEN
        CREATE INDEX CONCURRENTLY idx_bookings_event_status ON bookings (event_id, status);
    END IF;
END $$;

-- Index for seat availability queries (partial index for available seats > 0)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_events_available_seats') THEN
        CREATE INDEX CONCURRENTLY idx_events_available_seats ON events (available_seats) WHERE available_seats > 0;
    END IF;
END $$;

-- Index for upcoming events (without NOW() function - use regular index)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_events_starts_at') THEN
        CREATE INDEX CONCURRENTLY idx_events_starts_at ON events (starts_at);
    END IF;
END $$;

-- Index for confirmed bookings analytics
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_bookings_confirmed') THEN
        CREATE INDEX CONCURRENTLY idx_bookings_confirmed ON bookings (event_id, status) WHERE status = 'CONFIRMED';
    END IF;
END $$;

-- Add comments for documentation
COMMENT ON INDEX idx_events_available_seats IS 'Optimizes seat availability queries for concurrent booking operations';
COMMENT ON INDEX idx_events_starts_at IS 'Optimizes queries for upcoming events - filter by starts_at in application code';
COMMENT ON INDEX idx_bookings_confirmed IS 'Optimizes analytics queries for confirmed bookings';
COMMENT ON INDEX idx_bookings_idempotency_key IS 'Optimizes idempotency key lookups for duplicate prevention';
