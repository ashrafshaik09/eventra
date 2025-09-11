-- V2__AddUserAuthentication.sql
-- Add authentication and RBAC fields to users table

-- Add authentication columns to users table
ALTER TABLE users 
ADD COLUMN password_hash VARCHAR(255) NOT NULL DEFAULT 'temp_hash',
ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT true,
ADD COLUMN last_login TIMESTAMP WITH TIME ZONE,
ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- Remove the temporary default constraint
ALTER TABLE users ALTER COLUMN password_hash DROP DEFAULT;

-- Create index for role-based queries
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_active ON users (is_active);
CREATE INDEX idx_users_email_active ON users (email, is_active);

-- Update trigger for updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
