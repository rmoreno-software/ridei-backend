-- Catálogo de circuitos: venue (complejo) → track (pista por disciplina) → layout (trazado).
-- Nada se borra: los duplicados quedan con status = 'MERGED' y merged_into_id apuntando al
-- canónico, porque otros servicios referencian estos ids.
--
-- normalized_name lo calcula el dominio (NameNormalizer) y es la entrada de pg_trgm.

CREATE TABLE venue (
    id                UUID PRIMARY KEY,
    name              VARCHAR(150) NOT NULL,
    normalized_name   VARCHAR(150) NOT NULL,
    country_code      VARCHAR(2)   NOT NULL,
    latitude          DOUBLE PRECISION NOT NULL,
    longitude         DOUBLE PRECISION NOT NULL,
    -- Calculada por la base de datos; la entidad JPA no la mapea (no hace falta Hibernate Spatial).
    location          GEOGRAPHY(POINT, 4326)
                      GENERATED ALWAYS AS (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography) STORED,
    external_place_id VARCHAR(255),
    status            VARCHAR(20)  NOT NULL,
    merged_into_id    UUID REFERENCES venue (id),
    created_by        UUID,                 -- NULL = datos iniciales del catálogo
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    version           BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT ck_venue_country_code CHECK (country_code ~ '^[A-Z]{2}$'),
    CONSTRAINT ck_venue_latitude     CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_venue_longitude    CHECK (longitude BETWEEN -180 AND 180),
    CONSTRAINT ck_venue_status       CHECK (status IN ('PENDING_REVIEW', 'VERIFIED', 'MERGED')),
    CONSTRAINT ck_venue_merged_into  CHECK ((status = 'MERGED') = (merged_into_id IS NOT NULL)),
    CONSTRAINT ck_venue_not_self     CHECK (merged_into_id <> id)
);

-- Mismo lugar del proveedor de mapas ⇒ mismo venue (barrera final ante carreras).
CREATE UNIQUE INDEX uq_venue_external_place_id ON venue (external_place_id) WHERE external_place_id IS NOT NULL;
CREATE INDEX ix_venue_location             ON venue USING GIST (location);
CREATE INDEX ix_venue_normalized_name_trgm ON venue USING GIN (normalized_name gin_trgm_ops);
CREATE INDEX ix_venue_merged_into          ON venue (merged_into_id);


CREATE TABLE track (
    id              UUID PRIMARY KEY,
    venue_id        UUID         NOT NULL REFERENCES venue (id),
    discipline      VARCHAR(20)  NOT NULL,
    name            VARCHAR(150) NOT NULL,
    normalized_name VARCHAR(150) NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    merged_into_id  UUID REFERENCES track (id),
    created_at      TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_track_discipline  CHECK (discipline IN ('ROAD_RACING', 'KARTING', 'MOTOCROSS', 'SUPERMOTO', 'ENDURO', 'FLAT_TRACK')),
    CONSTRAINT ck_track_status      CHECK (status IN ('PENDING_REVIEW', 'VERIFIED', 'MERGED')),
    CONSTRAINT ck_track_merged_into CHECK ((status = 'MERGED') = (merged_into_id IS NOT NULL)),
    CONSTRAINT ck_track_not_self    CHECK (merged_into_id <> id)
);

-- Mismo venue + misma disciplina + mismo nombre normalizado ⇒ track duplicado.
CREATE UNIQUE INDEX uq_track_active_name ON track (venue_id, discipline, normalized_name) WHERE status <> 'MERGED';
CREATE INDEX ix_track_venue                ON track (venue_id);
CREATE INDEX ix_track_normalized_name_trgm ON track USING GIN (normalized_name gin_trgm_ops);


CREATE TABLE layout (
    id              UUID PRIMARY KEY,
    track_id        UUID         NOT NULL REFERENCES track (id),
    name            VARCHAR(150) NOT NULL,
    normalized_name VARCHAR(150) NOT NULL,
    length_m        INTEGER      NOT NULL,
    turns           INTEGER,
    direction       VARCHAR(20)  NOT NULL,
    is_default      BOOLEAN      NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    merged_into_id  UUID REFERENCES layout (id),
    created_at      TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_layout_length         CHECK (length_m > 0),
    CONSTRAINT ck_layout_turns          CHECK (turns IS NULL OR turns >= 0),
    CONSTRAINT ck_layout_direction      CHECK (direction IN ('CLOCKWISE', 'COUNTER_CLOCKWISE')),
    CONSTRAINT ck_layout_status         CHECK (status IN ('PENDING_REVIEW', 'VERIFIED', 'MERGED')),
    CONSTRAINT ck_layout_merged_into    CHECK ((status = 'MERGED') = (merged_into_id IS NOT NULL)),
    CONSTRAINT ck_layout_not_self       CHECK (merged_into_id <> id),
    CONSTRAINT ck_layout_merged_default CHECK (NOT (is_default AND status = 'MERGED'))
);

CREATE UNIQUE INDEX uq_layout_active_name ON layout (track_id, normalized_name) WHERE status <> 'MERGED';
CREATE INDEX ix_layout_track ON layout (track_id);

-- Un único layout por defecto por track. Es la versión diferible de
--   UNIQUE (track_id) WHERE is_default AND status <> 'MERGED'
-- (un índice único parcial no puede ser DEFERRABLE; una restricción de exclusión sí).
-- Cambiar el default son dos UPDATE (quitar el viejo, poner el nuevo) y Hibernate no
-- garantiza su orden dentro del flush: con una restricción inmediata, ejecutar primero el
-- del nuevo violaría la unicidad. Diferida, se comprueba al hacer commit. El dominio ya
-- garantiza la invariante y el bloqueo optimista del venue evita escrituras concurrentes.
ALTER TABLE layout
    ADD CONSTRAINT ex_layout_single_default
    EXCLUDE USING btree (track_id WITH =) WHERE (is_default AND status <> 'MERGED')
    DEFERRABLE INITIALLY DEFERRED;
