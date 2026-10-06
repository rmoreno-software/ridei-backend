CREATE TABLE motorbike_photos (
    id UUID PRIMARY KEY,
    motorbike_id UUID NOT NULL REFERENCES motorbikes(id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    focal_x DOUBLE PRECISION NOT NULL DEFAULT 0.5,
    focal_y DOUBLE PRECISION NOT NULL DEFAULT 0.5,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    uploaded_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_motorbike_photos_focal_x CHECK (focal_x BETWEEN 0 AND 1),
    CONSTRAINT chk_motorbike_photos_focal_y CHECK (focal_y BETWEEN 0 AND 1)
);

CREATE INDEX idx_motorbike_photos_motorbike_id ON motorbike_photos (motorbike_id);
