-- Datos iniciales: principales circuitos de España, VERIFIED, created_by NULL (sistema).
--
-- Criterio: solo se incluye lo contrastado en fuentes públicas (Wikipedia en/es/ca,
-- OpenStreetMap, fichas de circuito). Los layouts cuya longitud, curvas o sentido no
-- coinciden entre fuentes se han dejado fuera a propósito (ver docs/database.md, "Seed
-- pendiente de verificar"); se añadirán con una migración nueva una vez confirmados.
--
-- normalized_name DEBE coincidir con NameNormalizer.normalize(name); lo comprueba
-- SeedDataIT. Si cambian las reglas de normalización, hay que recalcularlo en una migración.
--
-- Ids fijos para que otros servicios y entornos compartan referencias:
--   venue  10000000-0000-4000-8000-0000000000NN
--   track  20000000-0000-4000-8000-0000000000NN
--   layout 30000000-0000-4000-8000-0000000000NN

INSERT INTO venue (id, name, normalized_name, country_code, latitude, longitude, status, created_at, updated_at, version) VALUES
    ('10000000-0000-4000-8000-000000000001', 'Circuit de Barcelona-Catalunya', 'barcelona catalunya',  'ES', 41.57000,   2.26111, 'VERIFIED', '2026-11-04T00:00:00Z', '2026-11-04T00:00:00Z', 0),
    ('10000000-0000-4000-8000-000000000002', 'Circuito de Jerez - Ángel Nieto', 'jerez angel nieto',   'ES', 36.70833,  -6.03417, 'VERIFIED', '2026-11-04T00:00:00Z', '2026-11-04T00:00:00Z', 0),
    ('10000000-0000-4000-8000-000000000003', 'MotorLand Aragón',               'motorland aragon',     'ES', 41.07833,  -0.20750, 'VERIFIED', '2026-11-04T00:00:00Z', '2026-11-04T00:00:00Z', 0),
    ('10000000-0000-4000-8000-000000000004', 'Circuit Ricardo Tormo',          'ricardo tormo',        'ES', 39.48583,  -0.62806, 'VERIFIED', '2026-11-04T00:00:00Z', '2026-11-04T00:00:00Z', 0),
    ('10000000-0000-4000-8000-000000000005', 'Circuito de Cartagena',          'cartagena',            'ES', 37.64444,  -1.03333, 'VERIFIED', '2026-11-04T00:00:00Z', '2026-11-04T00:00:00Z', 0),
    ('10000000-0000-4000-8000-000000000006', 'Circuito de Almería',            'almeria',              'ES', 37.08990,  -2.27190, 'VERIFIED', '2026-11-04T00:00:00Z', '2026-11-04T00:00:00Z', 0),
    ('10000000-0000-4000-8000-000000000007', 'Circuito de Navarra',            'navarra',              'ES', 42.56106,  -2.16361, 'VERIFIED', '2026-11-04T00:00:00Z', '2026-11-04T00:00:00Z', 0),
    ('10000000-0000-4000-8000-000000000008', 'Circuito de Albacete',           'albacete',             'ES', 39.00782,  -1.79590, 'VERIFIED', '2026-11-04T00:00:00Z', '2026-11-04T00:00:00Z', 0),
    ('10000000-0000-4000-8000-000000000009', 'Circuit de Calafat',             'calafat',              'ES', 40.93389,   0.84167, 'VERIFIED', '2026-11-04T00:00:00Z', '2026-11-04T00:00:00Z', 0),
    ('10000000-0000-4000-8000-000000000010', 'Circuit d''Alcarràs',            'alcarras',             'ES', 41.61933,   0.39889, 'VERIFIED', '2026-11-04T00:00:00Z', '2026-11-04T00:00:00Z', 0),
    ('10000000-0000-4000-8000-000000000011', 'Parcmotor Castellolí',           'parcmotor castelloli', 'ES', 41.59194,   1.68861, 'VERIFIED', '2026-11-04T00:00:00Z', '2026-11-04T00:00:00Z', 0);

INSERT INTO track (id, venue_id, discipline, name, normalized_name, status, created_at) VALUES
    -- Pistas de velocidad
    ('20000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000001', 'ROAD_RACING', 'Velocidad', 'velocidad', 'VERIFIED', '2026-11-04T00:00:00Z'),
    ('20000000-0000-4000-8000-000000000002', '10000000-0000-4000-8000-000000000002', 'ROAD_RACING', 'Velocidad', 'velocidad', 'VERIFIED', '2026-11-04T00:00:00Z'),
    ('20000000-0000-4000-8000-000000000003', '10000000-0000-4000-8000-000000000003', 'ROAD_RACING', 'Velocidad', 'velocidad', 'VERIFIED', '2026-11-04T00:00:00Z'),
    ('20000000-0000-4000-8000-000000000004', '10000000-0000-4000-8000-000000000004', 'ROAD_RACING', 'Velocidad', 'velocidad', 'VERIFIED', '2026-11-04T00:00:00Z'),
    ('20000000-0000-4000-8000-000000000005', '10000000-0000-4000-8000-000000000005', 'ROAD_RACING', 'Velocidad', 'velocidad', 'VERIFIED', '2026-11-04T00:00:00Z'),
    ('20000000-0000-4000-8000-000000000006', '10000000-0000-4000-8000-000000000006', 'ROAD_RACING', 'Velocidad', 'velocidad', 'VERIFIED', '2026-11-04T00:00:00Z'),
    ('20000000-0000-4000-8000-000000000007', '10000000-0000-4000-8000-000000000007', 'ROAD_RACING', 'Velocidad', 'velocidad', 'VERIFIED', '2026-11-04T00:00:00Z'),
    ('20000000-0000-4000-8000-000000000008', '10000000-0000-4000-8000-000000000008', 'ROAD_RACING', 'Velocidad', 'velocidad', 'VERIFIED', '2026-11-04T00:00:00Z'),
    ('20000000-0000-4000-8000-000000000009', '10000000-0000-4000-8000-000000000009', 'ROAD_RACING', 'Velocidad', 'velocidad', 'VERIFIED', '2026-11-04T00:00:00Z'),
    -- Alcarràs: autòdrom de velocidad, circuito mixto (supermotard y karting) y flat track, muy próximos
    ('20000000-0000-4000-8000-000000000010', '10000000-0000-4000-8000-000000000010', 'ROAD_RACING', 'Velocidad',      'velocidad', 'VERIFIED', '2026-11-04T00:00:00Z'),
    ('20000000-0000-4000-8000-000000000011', '10000000-0000-4000-8000-000000000010', 'KARTING',     'Circuito mixto', 'mixto',     'VERIFIED', '2026-11-04T00:00:00Z'),
    ('20000000-0000-4000-8000-000000000012', '10000000-0000-4000-8000-000000000010', 'FLAT_TRACK',  'Flat track',     'flat',      'VERIFIED', '2026-11-04T00:00:00Z'),
    -- Castellolí: velocidad, karting, motocross y enduro
    ('20000000-0000-4000-8000-000000000013', '10000000-0000-4000-8000-000000000011', 'ROAD_RACING', 'Velocidad', 'velocidad', 'VERIFIED', '2026-11-04T00:00:00Z'),
    ('20000000-0000-4000-8000-000000000014', '10000000-0000-4000-8000-000000000011', 'KARTING',     'Karting',   'karting',   'VERIFIED', '2026-11-04T00:00:00Z'),
    ('20000000-0000-4000-8000-000000000015', '10000000-0000-4000-8000-000000000011', 'MOTOCROSS',   'Motocross', 'motocross', 'VERIFIED', '2026-11-04T00:00:00Z'),
    ('20000000-0000-4000-8000-000000000016', '10000000-0000-4000-8000-000000000011', 'ENDURO',      'Enduro',    'enduro',    'VERIFIED', '2026-11-04T00:00:00Z');

-- Solo layouts con longitud, curvas y sentido coherentes entre fuentes.
INSERT INTO layout (id, track_id, name, normalized_name, length_m, turns, direction, is_default, status, created_at) VALUES
    ('30000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000001', 'GP',             'gp',             4657, 14, 'CLOCKWISE', TRUE,  'VERIFIED', '2026-11-04T00:00:00Z'),
    -- Jerez: trazado de motociclismo 4,423 km / 13 curvas; horario
    ('30000000-0000-4000-8000-000000000002', '20000000-0000-4000-8000-000000000002', 'GP',             'gp',             4423, 13, 'CLOCKWISE', TRUE,  'VERIFIED', '2026-11-04T00:00:00Z');
