CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(32) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    phone_number VARCHAR(20),
    date_of_birth DATE,
    country_code VARCHAR(2),
    document_type VARCHAR(30),
    document_number VARCHAR(30),
    role VARCHAR(20) NOT NULL,
    account_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    terms_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    terms_accepted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE rider_profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    profile_type VARCHAR(20),
    racing_license_number VARCHAR(50)
);

CREATE TABLE organizer_accounts (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    legal_name VARCHAR(150) NOT NULL,
    tax_id VARCHAR(30) NOT NULL,
    iban VARCHAR(34) NOT NULL
);
