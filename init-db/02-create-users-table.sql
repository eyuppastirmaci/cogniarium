-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    email_verification_token_expires_at TIMESTAMP,
    refresh_token VARCHAR(255),
    refresh_token_expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_login_at TIMESTAMP,
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
);

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Create index on email_verification_token for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_verification_token ON users(email_verification_token);

-- Create index on refresh_token for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_refresh_token ON users(refresh_token);

