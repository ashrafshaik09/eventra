-- Add new columns to events table
ALTER TABLE events ADD COLUMN description TEXT;
ALTER TABLE events ADD COLUMN tags VARCHAR(500);
ALTER TABLE events ADD COLUMN is_online BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE events ADD COLUMN image_url VARCHAR(1000);
ALTER TABLE events ADD COLUMN ends_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE events ADD COLUMN ticket_price DECIMAL(10,2) DEFAULT 0.00 NOT NULL;
ALTER TABLE events ADD COLUMN category_id UUID;

-- Create event_categories table
CREATE TABLE event_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    color_code VARCHAR(7), -- For UI theming (#FF5733)
    icon_name VARCHAR(50),  -- Icon identifier for frontend
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- Create event_likes table for user likes
CREATE TABLE event_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    event_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    CONSTRAINT fk_event_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_likes_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT uk_event_likes_user_event UNIQUE (user_id, event_id)
);

-- Create event_comments table
CREATE TABLE event_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    event_id UUID NOT NULL,
    comment_text TEXT NOT NULL,
    parent_comment_id UUID, -- For nested replies
    is_edited BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    CONSTRAINT fk_event_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_comments_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES event_comments(id) ON DELETE CASCADE
);

-- Create transactions table for detailed payment tracking
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL,
    user_id UUID NOT NULL,
    event_id UUID NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- 'PAYMENT', 'REFUND', 'PARTIAL_REFUND'
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD' NOT NULL,
    payment_method VARCHAR(50), -- 'CREDIT_CARD', 'PAYPAL', 'BANK_TRANSFER', etc.
    payment_gateway VARCHAR(50), -- 'STRIPE', 'PAYPAL', 'RAZORPAY', etc.
    gateway_transaction_id VARCHAR(255), -- External payment ID
    status VARCHAR(20) DEFAULT 'PENDING' NOT NULL, -- 'PENDING', 'COMPLETED', 'FAILED', 'CANCELLED'
    failure_reason TEXT,
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    CONSTRAINT fk_transactions_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_transactions_event FOREIGN KEY (event_id) REFERENCES events(id)
);

-- Add foreign key constraint for event categories
ALTER TABLE events ADD CONSTRAINT fk_events_category 
    FOREIGN KEY (category_id) REFERENCES event_categories(id) ON DELETE SET NULL;

-- Add booking_id column to link bookings to transactions
ALTER TABLE bookings ADD COLUMN total_amount DECIMAL(10,2);
ALTER TABLE bookings ADD COLUMN currency VARCHAR(3) DEFAULT 'USD';

-- Create indexes for performance
CREATE INDEX idx_events_category_id ON events(category_id);
CREATE INDEX idx_events_is_online ON events(is_online);
CREATE INDEX idx_events_price_range ON events(ticket_price);
CREATE INDEX idx_events_starts_at_ends_at ON events(starts_at, ends_at);

CREATE INDEX idx_event_likes_event_id ON event_likes(event_id);
CREATE INDEX idx_event_likes_user_id ON event_likes(user_id);

CREATE INDEX idx_event_comments_event_id ON event_comments(event_id);
CREATE INDEX idx_event_comments_user_id ON event_comments(user_id);
CREATE INDEX idx_event_comments_parent_id ON event_comments(parent_comment_id);

CREATE INDEX idx_transactions_booking_id ON transactions(booking_id);
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_event_id ON transactions(event_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

-- Insert default categories
INSERT INTO event_categories (name, description, color_code, icon_name) VALUES
    ('Music', 'Concerts, festivals, and musical performances', '#FF6B6B', 'music'),
    ('Sports', 'Sporting events and competitions', '#4ECDC4', 'sports'),
    ('Technology', 'Tech conferences, workshops, and meetups', '#45B7D1', 'tech'),
    ('Business', 'Corporate events, networking, and seminars', '#96CEB4', 'business'),
    ('Arts & Culture', 'Exhibitions, theater, and cultural events', '#FCEA2B', 'culture'),
    ('Food & Drink', 'Food festivals, wine tastings, and culinary events', '#FF9FF3', 'food'),
    ('Education', 'Workshops, courses, and educational seminars', '#54A0FF', 'education'),
    ('Health & Wellness', 'Fitness events, health seminars, and wellness workshops', '#5F27CD', 'health'),
    ('Entertainment', 'Comedy shows, movies, and entertainment events', '#00D2D3', 'entertainment'),
    ('Other', 'Miscellaneous events not fitting other categories', '#A4A4A4', 'other');
