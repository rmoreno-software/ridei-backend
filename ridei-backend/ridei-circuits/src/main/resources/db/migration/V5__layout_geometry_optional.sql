-- La longitud y el sentido de un layout los calcula el GPS con la primera sesión: un layout
-- recién propuesto puede no tenerlos todavía. Los CHECK existentes ya aceptan NULL.
ALTER TABLE layout ALTER COLUMN length_m DROP NOT NULL;
ALTER TABLE layout ALTER COLUMN direction DROP NOT NULL;
