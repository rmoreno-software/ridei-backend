-- Trazado de la pista de velocidad de Alcarràs (Autòdrom): dibujo, meta, longitud, curvas y
-- sentido. Las otras pistas del complejo (circuito mixto, flat track) siguen sin trazado.
--
-- ─────────────────────────────────────────────────────────────────────────────────────────
-- Geometría: © OpenStreetMap contributors, disponible bajo la Open Database License (ODbL)
-- https://www.openstreetmap.org/copyright — datos descargados el 2026-11-23 (API 0.6 /map).
--
-- Dibujo: relación type=circuit 21388960 ("The Alcarràs Speed Circuit"), vías 194653541 y
-- 1558804057 (ambas oneway=yes, encadenadas por sus nodos extremos 2051560828 y 2051559876).
-- Anillo de 160 nodos, 3740 m. Simplificado con LayoutPath.simplified (Douglas-Peucker 2,5 m)
-- empezando en la meta: 59 puntos, 3731 m frente a los 3743 m oficiales (-0,3 %).
--
-- Sentido: ANTIHORARIO. Lo dan los oneway=yes de OSM y coincide con Lapmeta ("CCW",
-- 14 curvas). Resuelve lo que quedó pendiente en V3, donde el sentido solo venía de Lapmeta.
-- La longitud oficial que se toma es 3743 m: el dibujo de OSM descarta los 4145 m de alguna
-- fuente. Curvas: 14 (Lapmeta y fichas consultadas para V3).
--
-- Meta: nodo 14178221871 (raceway=start-finish), sobre el eje de la pista. La puerta es
-- perpendicular a la marcha: 10 m hacia la izquierda y 6,15 m hacia la derecha, donde está el
-- pit lane (su eje, vía 194653524, a 12,3 m del de la pista). Así llega hasta el punto medio,
-- el muro de boxes, sin entrar en el pit lane, como en V7 y V8. El primer punto es el de la
-- izquierda mirando en el sentido de la marcha.
-- ─────────────────────────────────────────────────────────────────────────────────────────

INSERT INTO layout (id, track_id, name, normalized_name, length_m, turns, direction, is_default, status, created_at) VALUES
    ('30000000-0000-4000-8000-000000000008', '20000000-0000-4000-8000-000000000010', 'Principal', 'principal', 3743, 14, 'COUNTER_CLOCKWISE', TRUE, 'VERIFIED', '2026-11-23T00:00:00Z');

-- alcarras/Principal (59 puntos, empieza en la meta)
UPDATE layout SET
    finish_line = ST_GeomFromText('LINESTRING(
        0.402846 41.619355, 0.402953 41.619234)', 4326),
    path = ST_GeomFromText('LINESTRING(
        0.402912 41.619280, 0.405948 41.620779, 0.406035 41.620886, 0.406030 41.620975, 0.406017 41.621060, 0.405884 41.621215,
        0.405677 41.621332, 0.404688 41.621543, 0.404465 41.621567, 0.404163 41.621516, 0.401606 41.619319, 0.401531 41.619280,
        0.401261 41.619274, 0.401043 41.619406, 0.400931 41.619619, 0.401350 41.621118, 0.401407 41.621208, 0.401568 41.621312,
        0.402663 41.621494, 0.402869 41.621707, 0.402881 41.622043, 0.402787 41.622175, 0.402633 41.622276, 0.402303 41.622346,
        0.399847 41.622075, 0.399592 41.622002, 0.399367 41.621817, 0.398872 41.621144, 0.398759 41.620771, 0.398821 41.620468,
        0.399582 41.618839, 0.399632 41.618601, 0.399501 41.618357, 0.399280 41.618174, 0.399042 41.618074, 0.398684 41.618027,
        0.398414 41.618057, 0.398121 41.618169, 0.397866 41.618385, 0.397746 41.618644, 0.397773 41.618957, 0.398079 41.619501,
        0.398137 41.619683, 0.398044 41.619844, 0.397646 41.619983, 0.397583 41.620189, 0.398153 41.621502, 0.398134 41.621742,
        0.398031 41.621825, 0.397827 41.621875, 0.396689 41.621679, 0.396488 41.621524, 0.396413 41.621334, 0.397049 41.618111,
        0.397156 41.617935, 0.397884 41.617151, 0.398016 41.617069, 0.398259 41.617038, 0.398437 41.617084, 0.402912 41.619280)', 4326)
WHERE id = '30000000-0000-4000-8000-000000000008';
