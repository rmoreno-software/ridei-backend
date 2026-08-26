CREATE TABLE waitlist_entries (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT now()
);
