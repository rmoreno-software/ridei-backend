CREATE TABLE brands (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE UNIQUE INDEX uq_brands_name_lower ON brands (LOWER(name));
