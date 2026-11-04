package com.ridei.circuits.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import com.ridei.circuits.domain.model.DeduplicationPolicy;
import com.ridei.circuits.domain.model.Distance;
import com.ridei.circuits.domain.model.SimilarityThreshold;

/** {@code circuits.deduplication.*} en application.yml. */
@ConfigurationProperties("circuits.deduplication")
public record DeduplicationProperties(
    @DefaultValue("2") double venueRadiusKm,
    @DefaultValue("0.4") double venueMinSimilarity
) {

    public DeduplicationPolicy toPolicy() {
        return new DeduplicationPolicy(Distance.ofKilometers(venueRadiusKm), new SimilarityThreshold(venueMinSimilarity));
    }
}
