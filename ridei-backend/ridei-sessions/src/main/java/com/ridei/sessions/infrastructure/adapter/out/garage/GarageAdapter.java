package com.ridei.sessions.infrastructure.adapter.out.garage;

import java.util.List;
import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ridei.sessions.domain.exception.ExternalServiceUnavailableException;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.MotorbikeSnapshot;
import com.ridei.sessions.domain.port.out.GaragePort;
import com.ridei.sessions.infrastructure.adapter.out.http.CurrentBearerToken;
import com.ridei.sessions.infrastructure.adapter.out.http.UpstreamUnauthorizedException;

/**
 * {@code GET /api/v1/garage/motorbikes} de ridei-garage con el token del piloto: solo devuelve
 * sus motos, así la propiedad la decide el garaje (anti-IDOR).
 */
public class GarageAdapter implements GaragePort {

    private static final ParameterizedTypeReference<List<Motorbike>> MOTORBIKES = new ParameterizedTypeReference<>() {};

    private final RestClient garage;

    public GarageAdapter(RestClient garage) {
        this.garage = garage;
    }

    @Override
    public Optional<MotorbikeSnapshot> findOwnMotorbike(MotorbikeId motorbikeId) {
        List<Motorbike> motorbikes;
        try {
            motorbikes = garage.get()
                .uri("/api/v1/garage/motorbikes")
                .header(HttpHeaders.AUTHORIZATION, CurrentBearerToken.authorizationHeader())
                .retrieve()
                .body(MOTORBIKES);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) throw new UpstreamUnauthorizedException();
            throw new ExternalServiceUnavailableException("The garage", e);
        } catch (RestClientException e) {
            throw new ExternalServiceUnavailableException("The garage", e);
        }
        if (motorbikes == null) return Optional.empty();
        String id = motorbikeId.value().toString();
        return motorbikes.stream()
            .filter(m -> id.equalsIgnoreCase(m.id()))
            .findFirst()
            .map(m -> new MotorbikeSnapshot(motorbikeId, m.displayName(), m.category(), m.categoryGroup()));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Motorbike(String id, String brandName, String model, String category, String categoryGroup) {
        String displayName() {
            String name = ((brandName != null ? brandName : "") + " " + (model != null ? model : "")).trim();
            return name.isEmpty() ? "Moto" : name;
        }
    }
}
