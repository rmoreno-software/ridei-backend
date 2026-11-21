-- Sesiones (tandas) grabadas por los pilotos, sus vueltas y la telemetría bruta.
--
-- Las referencias a otros contextos (rider_id → ridei-identity, venue_id/layout_id →
-- ridei-circuits, motorbike_id → ridei-garage) son solo ids: sin FK entre bases de datos.
-- De cada trazado se congela lo necesario para cronometrar (meta y dibujo) en el momento de
-- subir la sesión, así los tiempos no cambian si el catálogo cambia (salvo recálculo explícito).

CREATE TABLE riding_session (
    id                       UUID PRIMARY KEY,
    rider_id                 UUID             NOT NULL,
    name                     VARCHAR(80)      NOT NULL,

    venue_id                 UUID             NOT NULL,
    venue_name               VARCHAR(200)     NOT NULL,
    track_name               VARCHAR(200),
    layout_id                UUID             NOT NULL,
    layout_name              VARCHAR(200)     NOT NULL,
    -- [[lat, lon], ...] en orden de recorrido, empezando en la meta, sin repetir el primero
    layout_path              JSONB            NOT NULL,
    -- [[lat, lon] izquierda, [lat, lon] derecha] mirando en el sentido de la marcha
    finish_line              JSONB            NOT NULL,
    sector_count             INTEGER          NOT NULL CHECK (sector_count BETWEEN 1 AND 10),

    motorbike_id             UUID             NOT NULL,
    motorbike_name           VARCHAR(200)     NOT NULL,
    motorbike_category       VARCHAR(40),
    motorbike_category_group VARCHAR(20),

    started_at               TIMESTAMPTZ      NOT NULL,
    ended_at                 TIMESTAMPTZ      NOT NULL,
    temperature_celsius      INTEGER,
    track_condition          VARCHAR(10)      CHECK (track_condition IN ('DRY', 'DAMP', 'WET')),
    notes                    VARCHAR(2000),

    reversed_direction       BOOLEAN          NOT NULL,
    -- desnormalizados para listados y récords sin recorrer las vueltas
    best_lap_ms              BIGINT,
    ideal_lap_ms             BIGINT,
    valid_lap_count          INTEGER          NOT NULL,

    created_at               TIMESTAMPTZ      NOT NULL,
    updated_at               TIMESTAMPTZ      NOT NULL,
    version                  BIGINT           NOT NULL DEFAULT 0
);

CREATE INDEX ix_riding_session_rider_started ON riding_session (rider_id, started_at DESC);
CREATE INDEX ix_riding_session_rider_layout_motorbike ON riding_session (rider_id, layout_id, motorbike_id);

CREATE TABLE session_lap (
    session_id      UUID             NOT NULL REFERENCES riding_session (id) ON DELETE CASCADE,
    lap_number      INTEGER          NOT NULL CHECK (lap_number >= 1),
    started_at      TIMESTAMPTZ      NOT NULL,
    duration_ms     BIGINT           NOT NULL CHECK (duration_ms > 0),
    -- un elemento por sector; NULL si no se detectó una de sus puertas
    sector_ms       BIGINT[]         NOT NULL,
    distance_m      DOUBLE PRECISION NOT NULL,
    validity        VARCHAR(20)      NOT NULL CHECK (validity IN ('VALID', 'TOO_SHORT', 'TOO_LONG', 'GPS_GAP')),
    PRIMARY KEY (session_id, lap_number)
);

-- Muestras GPS/IMU comprimidas (formato binario propio, ver TelemetryCodec). Se guardan para
-- recalcular vueltas (p. ej. si se corrige la meta) y para el análisis bajo demanda.
CREATE TABLE session_telemetry (
    session_id      UUID             PRIMARY KEY REFERENCES riding_session (id) ON DELETE CASCADE,
    encoding        VARCHAR(20)      NOT NULL,
    sample_count    INTEGER          NOT NULL,
    data            BYTEA            NOT NULL
);
