ALTER TABLE users
    ADD COLUMN temporary_password_hash VARCHAR(255),
    ADD COLUMN temporary_password_expires_at TIMESTAMP;
    