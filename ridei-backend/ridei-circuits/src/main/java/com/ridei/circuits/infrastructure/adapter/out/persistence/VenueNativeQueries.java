package com.ridei.circuits.infrastructure.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.ridei.circuits.domain.model.Distance;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.PageRequest;
import com.ridei.circuits.domain.model.SimilarityThreshold;
import com.ridei.circuits.domain.model.VenueName;
import com.ridei.circuits.domain.model.VenueSearchCriteria;

import lombok.AllArgsConstructor;

/**
 * Consultas que JPA no expresa: distancias PostGIS sobre la columna generada {@code location}
 * y similitud {@code pg_trgm} sobre {@code normalized_name}. Corren en la misma transacción
 * que JPA (JpaTransactionManager expone la conexión JDBC).
 */
@Component
@AllArgsConstructor
class VenueNativeQueries {

    private static final String POINT = "ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography";
    private static final String ACTIVE_TRACK_OF_VENUE = "t.venue_id = v.id AND t.status <> 'MERGED'";

    /** Tamaño de celda del bloqueo de zona; ~5,5 km de latitud, mayor que el radio de deduplicación. */
    static final double LOCK_CELL_DEGREES = 0.05;
    private static final double METERS_PER_DEGREE_OF_LATITUDE = 111_320;

    private final NamedParameterJdbcTemplate jdbc;

    record CandidateRow(UUID id, String name, String status, double distanceMeters, double similarity) {
    }

    record HitRow(UUID id, Double distanceMeters, Double similarity) {
    }

    record HitPage(List<HitRow> rows, long total) {
    }

    List<CandidateRow> potentialDuplicates(VenueName name, GeoPoint location, Distance radius, SimilarityThreshold min) {
        String sql = """
            SELECT v.id, v.name, v.status,
                   ST_Distance(v.location, %1$s) AS distance_m,
                   similarity(v.normalized_name, :name) AS similarity
            FROM venue v
            WHERE v.status <> 'MERGED'
              AND ST_DWithin(v.location, %1$s, :radius)
              AND similarity(v.normalized_name, :name) >= :minSimilarity
            ORDER BY similarity DESC, distance_m ASC
            LIMIT 10
            """.formatted(POINT);
        MapSqlParameterSource params = point(location)
            .addValue("name", name.normalized())
            .addValue("radius", radius.meters())
            .addValue("minSimilarity", min.value());
        return jdbc.query(sql, params, (rs, row) -> new CandidateRow(
            rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("status"),
            rs.getDouble("distance_m"), rs.getDouble("similarity")));
    }

    /**
     * Texto libre con {@code word_similarity} (operador {@code <%}, indexado por GIN): "jerez"
     * encuentra "jerez angel nieto", cosa que {@code similarity} sobre el nombre completo no haría.
     * Se busca en el nombre del venue y en los de sus tracks activos.
     */
    HitPage search(VenueSearchCriteria criteria, PageRequest page) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> conditions = new ArrayList<>(List.of("v.status <> 'MERGED'"));

        if (criteria.countryCode() != null) {
            conditions.add("v.country_code = :country");
            params.addValue("country", criteria.countryCode().value());
        }
        if (criteria.discipline() != null) {
            conditions.add("EXISTS (SELECT 1 FROM track t WHERE " + ACTIVE_TRACK_OF_VENUE + " AND t.discipline = :discipline)");
            params.addValue("discipline", criteria.discipline().name());
        }
        if (criteria.hasProximity()) {
            conditions.add("ST_DWithin(v.location, " + POINT + ", :radius)");
            params.addValues(point(criteria.near()).getValues()).addValue("radius", criteria.radius().meters());
        }
        if (criteria.hasText()) {
            conditions.add("(:q <% v.normalized_name OR EXISTS (SELECT 1 FROM track t WHERE "
                + ACTIVE_TRACK_OF_VENUE + " AND :q <% t.normalized_name))");
            params.addValue("q", criteria.normalizedText());
        }

        String distance = criteria.hasProximity() ? "ST_Distance(v.location, " + POINT + ")" : "NULL::float8";
        String similarity = criteria.hasText()
            ? "GREATEST(word_similarity(:q, v.normalized_name), COALESCE((SELECT MAX(word_similarity(:q, t.normalized_name))"
                + " FROM track t WHERE " + ACTIVE_TRACK_OF_VENUE + "), 0))"
            : "NULL::float8";
        String where = String.join(" AND ", conditions);

        String sql = """
            SELECT v.id, %s AS distance_m, %s AS similarity
            FROM venue v
            WHERE %s
            ORDER BY similarity DESC NULLS LAST, distance_m ASC NULLS LAST, v.name, v.id
            LIMIT :limit OFFSET :offset
            """.formatted(distance, similarity, where);
        return page(sql, "SELECT COUNT(*) FROM venue v WHERE " + where, params, page);
    }

    HitPage nearby(GeoPoint location, Distance radius, PageRequest page) {
        String where = "v.status <> 'MERGED' AND ST_DWithin(v.location, " + POINT + ", :radius)";
        String sql = """
            SELECT v.id, ST_Distance(v.location, %s) AS distance_m, NULL::float8 AS similarity
            FROM venue v
            WHERE %s
            ORDER BY distance_m ASC, v.id
            LIMIT :limit OFFSET :offset
            """.formatted(POINT, where);
        MapSqlParameterSource params = point(location).addValue("radius", radius.meters());
        return page(sql, "SELECT COUNT(*) FROM venue v WHERE " + where, params, page);
    }

    /**
     * Toma, en orden fijo (sin interbloqueos), un advisory lock de transacción por cada celda de
     * la rejilla que toca el cuadrado de lado 2·radius centrado en {@code location}. Dos
     * propuestas cuyas zonas se solapan comparten al menos una celda y se serializan.
     */
    void lockArea(GeoPoint location, Distance radius) {
        double latDelta = radius.meters() / METERS_PER_DEGREE_OF_LATITUDE;
        double lonDelta = latDelta / Math.max(Math.cos(Math.toRadians(location.latitude())), 0.01);
        for (int latCell = cell(location.latitude() - latDelta); latCell <= cell(location.latitude() + latDelta); latCell++) {
            for (int lonCell = cell(location.longitude() - lonDelta); lonCell <= cell(location.longitude() + lonDelta); lonCell++) {
                jdbc.queryForObject("SELECT 1 FROM pg_advisory_xact_lock(:lat, :lon)",
                    new MapSqlParameterSource().addValue("lat", latCell).addValue("lon", lonCell), Integer.class);
            }
        }
    }

    private static int cell(double degrees) {
        return (int) Math.floor(degrees / LOCK_CELL_DEGREES);
    }

    private HitPage page(String sql, String countSql, MapSqlParameterSource params, PageRequest page) {
        params.addValue("limit", page.size()).addValue("offset", page.offset());
        List<HitRow> rows = jdbc.query(sql, params, (rs, row) -> new HitRow(
            rs.getObject("id", UUID.class),
            nullableDouble(rs.getObject("distance_m")),
            nullableDouble(rs.getObject("similarity"))));
        Long total = jdbc.queryForObject(countSql, params, Long.class);
        return new HitPage(rows, total == null ? 0 : total);
    }

    /** {@code similarity()}/{@code word_similarity()} devuelven {@code real} (Float) y ST_Distance {@code float8}. */
    private static Double nullableDouble(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
    }

    private static MapSqlParameterSource point(GeoPoint location) {
        return new MapSqlParameterSource().addValue("lat", location.latitude()).addValue("lon", location.longitude());
    }
}
