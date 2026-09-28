-- Corrige el nombre canónico de marcas ya existentes que V5 insertó con un nombre distinto
UPDATE brands SET name = 'BMW' WHERE LOWER(name) = LOWER('BMW Motorrad');
UPDATE brands SET name = 'Indian' WHERE LOWER(name) = LOWER('Indian Motorcycle');
UPDATE brands SET name = 'CFMoto' WHERE LOWER(name) = LOWER('CFMOTO');

-- Elimina marcas que no forman parte de la lista definitiva.
-- Si alguna moto ya referenciase una de estas por brand_id, esto falla (FK sin ON DELETE)
-- en vez de dejar datos huérfanos en silencio — comportamiento intencionado.
DELETE FROM brands WHERE LOWER(name) IN (
    LOWER('Vespa'),
    LOWER('Piaggio'),
    LOWER('Kymco'),
    LOWER('SYM'),
    LOWER('Bultaco')
);

-- Añade el resto de la lista definitiva que todavía no existía.
-- ON CONFLICT sobre el índice único de LOWER(name) hace la migración segura de re-ejecutar
-- mentalmente / a prueba de solapamientos con lo que ya insertó V5.
INSERT INTO brands (id, name) VALUES
    (gen_random_uuid(), 'AJP'),
    (gen_random_uuid(), 'Apollo'),
    (gen_random_uuid(), 'Arc'),
    (gen_random_uuid(), 'Ariel'),
    (gen_random_uuid(), 'Ather'),
    (gen_random_uuid(), 'Bajaj'),
    (gen_random_uuid(), 'Bimota'),
    (gen_random_uuid(), 'Blata'),
    (gen_random_uuid(), 'Brough Superior'),
    (gen_random_uuid(), 'BSE'),
    (gen_random_uuid(), 'Bucci Moto'),
    (gen_random_uuid(), 'Buell'),
    (gen_random_uuid(), 'Cake'),
    (gen_random_uuid(), 'Can-Am'),
    (gen_random_uuid(), 'Cobra'),
    (gen_random_uuid(), 'Damon'),
    (gen_random_uuid(), 'Electric Motion'),
    (gen_random_uuid(), 'Evoke'),
    (gen_random_uuid(), 'Fuell'),
    (gen_random_uuid(), 'GRC'),
    (gen_random_uuid(), 'Horwin'),
    (gen_random_uuid(), 'Husaberg'),
    (gen_random_uuid(), 'Hyosung'),
    (gen_random_uuid(), 'IMR'),
    (gen_random_uuid(), 'Kalk'),
    (gen_random_uuid(), 'Kayo'),
    (gen_random_uuid(), 'Keeway'),
    (gen_random_uuid(), 'Kove'),
    (gen_random_uuid(), 'Kuberg'),
    (gen_random_uuid(), 'LEM'),
    (gen_random_uuid(), 'Lightning'),
    (gen_random_uuid(), 'LiveWire'),
    (gen_random_uuid(), 'Maeving'),
    (gen_random_uuid(), 'Malcor'),
    (gen_random_uuid(), 'Metrakit'),
    (gen_random_uuid(), 'Moto Morini'),
    (gen_random_uuid(), 'Niu'),
    (gen_random_uuid(), 'Norton'),
    (gen_random_uuid(), 'Ohvale'),
    (gen_random_uuid(), 'Ola Electric'),
    (gen_random_uuid(), 'Oset'),
    (gen_random_uuid(), 'Pitster Pro'),
    (gen_random_uuid(), 'Polini'),
    (gen_random_uuid(), 'QJ Motor'),
    (gen_random_uuid(), 'Ryvid'),
    (gen_random_uuid(), 'Scorpa'),
    (gen_random_uuid(), 'Segway'),
    (gen_random_uuid(), 'Silence'),
    (gen_random_uuid(), 'Stark Future'),
    (gen_random_uuid(), 'Stomp'),
    (gen_random_uuid(), 'Super Soco'),
    (gen_random_uuid(), 'Sur-Ron'),
    (gen_random_uuid(), 'SWM'),
    (gen_random_uuid(), 'Talaria'),
    (gen_random_uuid(), 'Thumpstar'),
    (gen_random_uuid(), 'TM Racing'),
    (gen_random_uuid(), 'Torrot'),
    (gen_random_uuid(), 'TRRS'),
    (gen_random_uuid(), 'TVS'),
    (gen_random_uuid(), 'Ultraviolette'),
    (gen_random_uuid(), 'Verge'),
    (gen_random_uuid(), 'Vertigo'),
    (gen_random_uuid(), 'Vmoto'),
    (gen_random_uuid(), 'Voge'),
    (gen_random_uuid(), 'Volcon'),
    (gen_random_uuid(), 'Vyrus'),
    (gen_random_uuid(), 'WPB'),
    (gen_random_uuid(), 'YCF'),
    (gen_random_uuid(), 'Zontes')
ON CONFLICT ((LOWER(name))) DO NOTHING;
