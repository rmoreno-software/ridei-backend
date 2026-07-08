package com.ridei.identity.infrastructure.adapter.out.google;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.ridei.identity.domain.exception.InvalidGoogleTokenException;
import com.ridei.identity.domain.model.GoogleUserInfo;
import com.ridei.identity.domain.port.out.GoogleTokenVerifierPort;

@Component
public class GoogleTokenVerifierAdapter implements GoogleTokenVerifierPort {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifierAdapter.class);
    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierAdapter(@Value("${google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
            new NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(Collections.singletonList(clientId))
            .build();
    }

    @Override
    public GoogleUserInfo verify(String idToken) {
        try {
            log.info("Verifying Google token, length: {}", idToken.length());
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                log.error("Google returned null - token invalid or expired");
                throw new InvalidGoogleTokenException("Invalid or expired Google token");
            }      
            
            GoogleIdToken.Payload payload = token.getPayload();
            return new GoogleUserInfo(
                payload.getSubject(),
                payload.getEmail(),
                (String) payload.get("given_name"),
                (String) payload.get("family_name"),
                (String) payload.get("picture")
            );
        } catch (InvalidGoogleTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception verifying token: {}", e.getMessage(), e);
            throw new InvalidGoogleTokenException("Failed to verify Google token: " + e.getMessage());
        }
    }
    
}
