package com.ridei.circuits.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.ridei.circuits.infrastructure.adapter.out.identity.CachingTokenValidator;
import com.ridei.circuits.infrastructure.adapter.out.identity.IdentityTokenValidationClient;
import com.ridei.circuits.infrastructure.adapter.out.identity.TokenValidator;

@Configuration
public class IdentityClientConfig {

    @Bean
    public TokenValidator tokenValidator(IdentityClientProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());

        RestClient identity = RestClient.builder()
            .baseUrl(properties.baseUrl())
            .requestFactory(requestFactory)
            .build();

        return new CachingTokenValidator(
            new IdentityTokenValidationClient(identity),
            properties.validationCacheTtl(),
            properties.validationCacheMaxSize()
        );
    }
}
