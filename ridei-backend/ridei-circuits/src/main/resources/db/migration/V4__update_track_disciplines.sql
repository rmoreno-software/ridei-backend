-- Tipologías de pista: se añaden TRIAL, MIXED y DIRT_TRACK y desaparece
-- SUPERMOTO. Las pistas de supermotard que ya existan pasan a MIXED, que es
-- la tipología que combina asfalto y tierra.

ALTER TABLE track DROP CONSTRAINT ck_track_discipline;

UPDATE track SET discipline = 'MIXED' WHERE discipline = 'SUPERMOTO';

ALTER TABLE track ADD CONSTRAINT ck_track_discipline
    CHECK (discipline IN ('ROAD_RACING', 'KARTING', 'ENDURO', 'TRIAL', 'MOTOCROSS', 'MIXED', 'FLAT_TRACK', 'DIRT_TRACK'));
