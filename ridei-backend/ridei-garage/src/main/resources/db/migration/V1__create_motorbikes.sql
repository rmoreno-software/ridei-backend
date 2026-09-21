-- owner_id referencia a un usuario de otro servicio (ridei_identity). Sin FK a propósito: son bases de datos distintas.
CREATE TABLE motorbikes (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    displacement_cc INTEGER,
    weight_kg NUMERIC(6,2),
    acquisition_date DATE,
    disposal_date DATE,
    photo_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_motorbikes_disposal_after_acquisition
        CHECK (disposal_date IS NULL OR acquisition_date IS NULL OR disposal_date >= acquisition_date)
);

CREATE INDEX idx_motorbikes_owner_id ON motorbikes (owner_id);
