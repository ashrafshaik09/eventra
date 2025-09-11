-- V5__CreateWaitlistTable.sql
-- Create waitlist table for FIFO queue functionality

CREATE TABLE waitlist (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    event_id UUID NOT NULL,
    position INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    notified_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    
    -- Foreign key constraints
    CONSTRAINT fk_waitlist_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_waitlist_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    
    -- Unique constraints
    CONSTRAINT uk_waitlist_user_event UNIQUE (user_id, event_id),
    
    -- Check constraints
    CONSTRAINT chk_waitlist_position_positive CHECK (position > 0),
    CONSTRAINT chk_waitlist_status_valid CHECK (status IN ('WAITING', 'NOTIFIED', 'EXPIRED', 'CONVERTED'))
);

-- Create indexes for performance
CREATE INDEX idx_waitlist_event_position ON waitlist (event_id, position) WHERE status = 'WAITING';
CREATE INDEX idx_waitlist_status ON waitlist (status);
CREATE INDEX idx_waitlist_expires_at ON waitlist (expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_waitlist_user_created ON waitlist (user_id, created_at);

-- Add comments for documentation
COMMENT ON TABLE waitlist IS 'FIFO queue for users waiting for sold-out events';
COMMENT ON COLUMN waitlist.position IS 'Position in queue (1 = first in line)';
COMMENT ON COLUMN waitlist.status IS 'WAITING, NOTIFIED, EXPIRED, or CONVERTED';
COMMENT ON COLUMN waitlist.notified_at IS 'When user was notified of available seat';
COMMENT ON COLUMN waitlist.expires_at IS 'When booking window expires (10 minutes from notification)';
