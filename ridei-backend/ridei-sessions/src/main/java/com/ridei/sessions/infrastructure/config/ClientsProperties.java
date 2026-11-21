package com.ridei.sessions.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Dónde están ridei-circuits y ridei-garage, y cuánto esperarlos. */
@ConfigurationProperties(prefix = "sessions.clients")
public record ClientsProperties(String circuitsBaseUrl, String garageBaseUrl, Duration connectTimeout, Duration readTimeout) {}
