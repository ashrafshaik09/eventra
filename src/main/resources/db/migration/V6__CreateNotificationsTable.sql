-- V6__CreateNotificationsTable.sql
-- Create notifications table for in-app notifications

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    action_url VARCHAR(500),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    read_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    metadata TEXT,
    
    -- Foreign key constraints
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    
    -- Check constraints
    CONSTRAINT chk_notification_type_valid CHECK (type IN ('BOOKING_CONFIRMED', 'BOOKING_CANCELLED', 'WAITLIST_SEAT_AVAILABLE', 'EVENT_UPDATED', 'GENERAL'))
);

-- Create indexes for performance
CREATE INDEX idx_notifications_user_read ON notifications (user_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications (created_at);
CREATE INDEX idx_notifications_expires_at ON notifications (expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_notifications_type ON notifications (type);

-- Add comments for documentation
COMMENT ON TABLE notifications IS 'In-app notifications for users';
COMMENT ON COLUMN notifications.type IS 'Type of notification (BOOKING_CONFIRMED, WAITLIST_SEAT_AVAILABLE, etc.)';
COMMENT ON COLUMN notifications.action_url IS 'URL to navigate when notification is clicked';
COMMENT ON COLUMN notifications.expires_at IS 'When notification expires (for time-sensitive notifications)';
COMMENT ON COLUMN notifications.metadata IS 'JSON metadata for additional notification data';
