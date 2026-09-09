ALTER TABLE users
    ADD COLUMN email_verification_token_hash VARCHAR(64),
    ADD COLUMN email_verification_token_expires_at TIMESTAMP;