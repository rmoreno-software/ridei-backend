package com.ridei.circuits.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** {@code circuits.identity.*} en application.yml. */
@ConfigurationProperties("circuits.identity")
public record IdentityClientProperties(
    @DefaultValue("http://localhost:8081") String baseUrl,
    @DefaultValue("1s") Duration connectTimeout,
    @DefaultValue("2s") Duration readTimeout,
    @DefaultValue("60s") Duration validationCacheTtl,
    @DefaultValue("10000") long validationCacheMaxSize
) {
}
