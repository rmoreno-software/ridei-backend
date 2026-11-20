-- Línea de meta de Calafat (trazado Principal).
--
-- Punto de meta facilitado por el equipo: 40°55'56.0"N 0°50'21.9"E (40.932222, 0.839417), a
-- 0,8 m del eje de la pista de OSM (way 119486523) y 82 m después del inicio de la recta
-- principal. Se proyecta sobre el eje (40.932217, 0.839424) y la puerta se traza
-- perpendicular a la marcha: 10 m hacia la izquierda y 6,5 m hacia la derecha, donde está el
-- pit lane (su eje, way 119486546, a 12,9 m del de la pista): llega hasta el punto medio, el
-- muro de boxes, sin entrar en el pit lane. Como en V7, el primer punto es el de la
-- izquierda mirando en el sentido de la marcha.
--
-- El dibujo se recalcula empezando en la meta (62 puntos, 3240 m frente a 3250).
--
-- Sentido: se mantiene HORARIO (V6). "La primera curva es a izquierdas" (equipo) encaja: en
-- horario, al final de la recta hay una curva suave a izquierdas (~26°) y después un ángulo a
-- derechas; es también el sentido que respaldan las 9 der. / 7 izq. oficiales y el orden de
-- los atajos de V6.
--
-- Geometría de la pista y del pit lane: © OpenStreetMap contributors, ODbL.

UPDATE layout SET
    finish_line = ST_GeomFromText('LINESTRING(
        0.839336 40.932277, 0.839481 40.932178)', 4326),
    path = ST_GeomFromText('LINESTRING(
        0.839424 40.932217, 0.843456 40.935607, 0.843801 40.936683, 0.843900 40.936788, 0.844098 40.936817, 0.844212 40.936765,
        0.844264 40.936678, 0.844337 40.936243, 0.844285 40.935892, 0.844208 40.935711, 0.844067 40.935484, 0.843776 40.935171,
        0.842278 40.933920, 0.842216 40.933770, 0.842356 40.933600, 0.842563 40.933572, 0.842711 40.933623, 0.844493 40.935117,
        0.844640 40.935339, 0.844642 40.935733, 0.844832 40.935952, 0.845182 40.936180, 0.845348 40.936246, 0.845608 40.936245,
        0.845846 40.936124, 0.845933 40.935924, 0.845864 40.935709, 0.844730 40.934516, 0.843546 40.933490, 0.843366 40.933385,
        0.842967 40.933259, 0.842786 40.933163, 0.842748 40.933046, 0.842866 40.932713, 0.842821 40.932545, 0.842067 40.931741,
        0.841874 40.931662, 0.841629 40.931659, 0.841455 40.931733, 0.841024 40.932036, 0.840872 40.932083, 0.840635 40.932087,
        0.840332 40.931924, 0.840056 40.931622, 0.839608 40.931501, 0.839487 40.931419, 0.839187 40.930971, 0.839107 40.930768,
        0.838998 40.929359, 0.838964 40.929245, 0.838873 40.929159, 0.838720 40.929128, 0.838564 40.929153, 0.836832 40.930406,
        0.836766 40.930493, 0.836747 40.930653, 0.836839 40.930850, 0.837013 40.930995, 0.837181 40.931077, 0.837966 40.931288,
        0.838393 40.931449, 0.838771 40.931674, 0.839424 40.932217)', 4326)
WHERE id = '30000000-0000-4000-8000-000000000006';
