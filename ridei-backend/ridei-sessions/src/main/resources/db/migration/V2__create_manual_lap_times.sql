-- Tiempos apuntados a mano de tandas anteriores a Ridei (o sin el móvil): pantallas 22–22E.
--
-- Como una sesión, van a un trazado y a una moto (solo ids, sin FK entre bases de datos, con el
-- nombre congelado), pero sin GPS: no hay meta, dibujo, sectores ni telemetría. Cuentan para el
-- historial, la evolución y el mejor tiempo manual, nunca para la vuelta ideal ni los sectores.

CREATE TABLE manual_lap_time (
    id                       UUID          PRIMARY KEY,
    rider_id                 UUID          NOT NULL,

    venue_id                 UUID          NOT NULL,
    venue_name               VARCHAR(200)  NOT NULL,
    track_name               VARCHAR(200),
    layout_id                UUID          NOT NULL,
    layout_name              VARCHAR(200)  NOT NULL,

    motorbike_id             UUID          NOT NULL,
    motorbike_name           VARCHAR(200)  NOT NULL,
    motorbike_category       VARCHAR(40),
    motorbike_category_group VARCHAR(20),

    -- solo el día: de una tanda antigua rara vez se sabe la hora
    lap_date                 DATE          NOT NULL,
    best_lap_ms              BIGINT        NOT NULL CHECK (best_lap_ms BETWEEN 10000 AND 3600000),
    -- con qué precisión se apuntó (1:52 · 1:49.8 · 1:14.22 · 1:45.300), para mostrarlo tal cual
    lap_precision            VARCHAR(12)   NOT NULL CHECK (lap_precision IN ('SECONDS', 'TENTHS', 'HUNDREDTHS', 'THOUSANDTHS')),
    time_source              VARCHAR(20)   NOT NULL CHECK (time_source IN ('OFFICIAL_TIMING', 'LAPTIMER', 'STOPWATCH', 'APPROXIMATE')),

    -- "Más datos · opcional"
    lap_count                INTEGER       CHECK (lap_count BETWEEN 1 AND 500),
    track_condition          VARCHAR(10)   CHECK (track_condition IN ('DRY', 'DAMP', 'WET')),
    tyres                    VARCHAR(100),
    other_laps_ms            BIGINT[]      NOT NULL DEFAULT '{}',
    notes                    VARCHAR(2000),

    created_at               TIMESTAMPTZ   NOT NULL
);

CREATE INDEX ix_manual_lap_time_rider_date ON manual_lap_time (rider_id, lap_date DESC);
CREATE INDEX ix_manual_lap_time_rider_layout_motorbike ON manual_lap_time (rider_id, layout_id, motorbike_id);
