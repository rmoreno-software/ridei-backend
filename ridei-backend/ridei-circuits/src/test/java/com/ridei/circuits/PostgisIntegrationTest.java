package com.ridei.circuits;

import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.ridei.circuits.domain.model.GeoPoint;

/**
 * Base de los tests de integración (*IT, ejecutados por failsafe en {@code mvn verify}):
 * contexto Spring completo contra un PostgreSQL con PostGIS real, migrado por Flyway con el
 * seed incluido. Un único contenedor compartido por todas las clases.
 *
 * <p>Los tests no limpian la base de datos: cada uno crea venues en coordenadas aleatorias en
 * mitad del océano para no interferir con el seed ni entre sí.
 */
@SpringBootTest
public abstract class PostgisIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGIS = new PostgreSQLContainer<>(
        DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres"));

    static {
        POSTGIS.start();
    }

    @DynamicPropertySource
    static void jwtPublicKey(DynamicPropertyRegistry registry) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        String publicKey = Base64.getEncoder().encodeToString(generator.generateKeyPair().getPublic().getEncoded());
        registry.add("jwt.public-key", () -> publicKey);
    }

    /** Punto aleatorio en el Atlántico Sur, lejos de cualquier venue real y de otros tests. */
    protected static GeoPoint somewhereInTheOcean() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return GeoPoint.of(-30 + random.nextDouble(-15, 15), -20 + random.nextDouble(-15, 15));
    }

    /** Punto a {@code meters} al norte de {@code origin}. */
    protected static GeoPoint northOf(GeoPoint origin, double meters) {
        return GeoPoint.of(origin.latitude() + meters / 111_195.0, origin.longitude());
    }
}
