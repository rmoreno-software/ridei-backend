-- Histórico del setup de cada moto: un periodo por pieza montada (neumáticos y relación final).
-- removed_on NULL = pieza montada actualmente.
CREATE TABLE motorbike_setup_entries (
    id UUID PRIMARY KEY,
    motorbike_id UUID NOT NULL REFERENCES motorbikes(id) ON DELETE CASCADE,
    component VARCHAR(20) NOT NULL,
    tyre_model VARCHAR(100),
    tyre_size VARCHAR(30),
    front_sprocket INTEGER,
    rear_sprocket INTEGER,
    mounted_on DATE NOT NULL,
    removed_on DATE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_setup_entries_component
        CHECK (component IN ('FRONT_TYRE', 'REAR_TYRE', 'FINAL_DRIVE')),
    CONSTRAINT chk_setup_entries_removed_after_mounted
        CHECK (removed_on IS NULL OR removed_on >= mounted_on),
    -- Cada componente lleva solo los campos de su tipo de especificación
    CONSTRAINT chk_setup_entries_spec_matches_component CHECK (
        (component IN ('FRONT_TYRE', 'REAR_TYRE')
            AND tyre_model IS NOT NULL
            AND front_sprocket IS NULL AND rear_sprocket IS NULL)
        OR
        (component = 'FINAL_DRIVE'
            AND tyre_model IS NULL AND tyre_size IS NULL
            AND front_sprocket > 0 AND rear_sprocket > 0)
    )
);

-- Una sola pieza montada por componente; también protege frente a dos cambios concurrentes.
CREATE UNIQUE INDEX uq_setup_entries_current
    ON motorbike_setup_entries (motorbike_id, component)
    WHERE removed_on IS NULL;

-- Dos piezas no pueden montarse el mismo día en el mismo componente (y sirve de índice por moto).
CREATE UNIQUE INDEX uq_setup_entries_mounted_on
    ON motorbike_setup_entries (motorbike_id, component, mounted_on);
