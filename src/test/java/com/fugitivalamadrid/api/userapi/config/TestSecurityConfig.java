package com.fugitivalamadrid.api.userapi.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.Instant;
import java.util.Map;

/**
 * Test security configuration that provides a mock JWT decoder for testing.
 * This configuration is used only in test profiles to avoid connecting to real Keycloak.
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * Creates a mock JWT decoder that doesn't require network connectivity for tests.
     * This allows tests to run without depending on a real Keycloak server.
     */
    @Bean
    @Primary
    public JwtDecoder mockJwtDecoder() {
        return new MockJwtDecoder();
    }

    private static class MockJwtDecoder implements JwtDecoder {
        @Override
        public Jwt decode(String token) throws JwtException {
            // For testing, we don't validate the issuer since we don't have a real Keycloak
            // The production decoder will enforce issuer validation
            return Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("scope", "read write")
                    .claim("realm_access", Map.of("roles", java.util.List.of("ADMIN")))
                    .claim("sub", "test-user")
                    .claim("iss", "http://localhost:8180/realms/userapi-realm")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
        }
    }
}
