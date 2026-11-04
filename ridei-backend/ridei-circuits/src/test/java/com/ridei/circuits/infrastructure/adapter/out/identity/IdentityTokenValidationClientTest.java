package com.ridei.circuits.infrastructure.adapter.out.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class IdentityTokenValidationClientTest {

    private static final String URL = "http://identity" + IdentityTokenValidationClient.VALIDATE_PATH;

    private final RestClient.Builder builder = RestClient.builder().baseUrl("http://identity");
    private final MockRestServiceServer identity = MockRestServiceServer.bindTo(builder).build();
    private final IdentityTokenValidationClient client = new IdentityTokenValidationClient(builder.build());

    @Test
    void forwardsTheTokenAndAcceptsItWhenIdentitySays200() {
        identity.expect(requestTo(URL)).andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer token-1"))
            .andRespond(withSuccess());

        assertThat(client.validate("token-1")).isEqualTo(TokenValidationStatus.VALID);
        identity.verify();
    }

    @Test
    void unauthorizedMeansRevoked() {
        identity.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThat(client.validate("token-1")).isEqualTo(TokenValidationStatus.REVOKED);
    }

    @Test
    void serverErrorsMeanUnavailable() {
        identity.expect(requestTo(URL)).andRespond(withServerError());

        assertThat(client.validate("token-1")).isEqualTo(TokenValidationStatus.UNAVAILABLE);
    }

    @Test
    void networkErrorsMeanUnavailable() {
        identity.expect(requestTo(URL)).andRespond(withException(new SocketTimeoutException("read timed out")));

        assertThat(client.validate("token-1")).isEqualTo(TokenValidationStatus.UNAVAILABLE);
    }

    @Test
    void connectionRefusedMeansUnavailable() {
        identity.expect(requestTo(URL)).andRespond(withException(new IOException("Connection refused")));

        assertThat(client.validate("token-1")).isEqualTo(TokenValidationStatus.UNAVAILABLE);
    }
}
