-- PostGIS: columnas geography y consultas por distancia (ST_DWithin / ST_Distance).
-- pg_trgm: similitud por trigramas sobre normalized_name.
--
-- pg_trgm es una extensión "trusted" y la puede crear el propietario de la base de datos,
-- pero postgis NO: en entornos donde Flyway corre con un rol sin superusuario (prod), un
-- superusuario debe crearla antes al provisionar la base de datos (ver docs/deployment.md).
-- IF NOT EXISTS hace que esta migración sea un no-op en ese caso.
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
