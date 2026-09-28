ALTER TABLE motorbikes DROP COLUMN brand;

ALTER TABLE motorbikes ADD COLUMN brand_id UUID REFERENCES brands(id);
ALTER TABLE motorbikes ADD COLUMN brand_name VARCHAR(100) NOT NULL;
