package com.fugitivalamadrid.api.userapi.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Security Configuration Tests")
class SecurityConfigTest {

    @Mock
    private JwtAuthConverter jwtAuthConverter;

    @Mock
    private Environment environment;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    @DisplayName("SecurityConfig should be created with dependencies")
    void securityConfigCreation() {
        assertNotNull(securityConfig);
        assertNotNull(jwtAuthConverter);
        assertNotNull(environment);
    }

    @Test
    @DisplayName("JwtDecoder should be created with default issuer URI")
    void jwtDecoderCreationWithDefaultIssuer() {
        when(environment.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri"))
                .thenReturn(null);
        
        JwtDecoder jwtDecoder = securityConfig.jwtDecoder(environment);
        
        assertNotNull(jwtDecoder);
        assertTrue(jwtDecoder instanceof NimbusJwtDecoder);
    }

    @Test
    @DisplayName("JwtDecoder should be created with custom issuer URI")
    void jwtDecoderCreationWithCustomIssuer() {
        when(environment.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri"))
                .thenReturn("http://localhost:8180/realms/userapi-realm");
        
        JwtDecoder jwtDecoder = securityConfig.jwtDecoder(environment);
        
        assertNotNull(jwtDecoder);
        assertTrue(jwtDecoder instanceof NimbusJwtDecoder);
    }

    @Test
    @DisplayName("JwtAuthConverter should correctly extract roles and scopes")
    void jwtAuthConverterShouldExtractRolesAndScopes() {
        // Create a JWT with roles and scopes
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("scope", List.of("read", "write"))
                .claim("realm_access", Map.of("roles", List.of("ADMIN", "USER")))
                .claim("sub", "test-user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // Create a real JwtAuthConverter to test the actual logic
        JwtAuthConverter realConverter = new JwtAuthConverter();
        var authToken = realConverter.convert(jwt);
        
        assertNotNull(authToken);
        
        List<GrantedAuthority> authorities = (List<GrantedAuthority>) authToken.getAuthorities();
        
        // Check for scope authorities
        assertTrue(
            authorities.stream().anyMatch(a -> a.getAuthority().equals("SCOPE_read"))
        );
        assertTrue(
            authorities.stream().anyMatch(a -> a.getAuthority().equals("SCOPE_write"))
        );
        
        // Check for role authorities
        assertTrue(
            authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
        );
        assertTrue(
            authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
        );
    }

    @Test
    @DisplayName("Security configuration constants should have correct values")
    void securityConfigConstantsShouldHaveCorrectValues() {
        // Use reflection to test private constants
        try {
            java.lang.reflect.Field usersEndpointField = SecurityConfig.class.getDeclaredField("USERS_ENDPOINT");
            usersEndpointField.setAccessible(true);
            String usersEndpoint = (String) usersEndpointField.get(null);
            org.junit.jupiter.api.Assertions.assertEquals("/users/**", usersEndpoint);

            java.lang.reflect.Field adminRoleField = SecurityConfig.class.getDeclaredField("ADMIN_ROLE");
            adminRoleField.setAccessible(true);
            String adminRole = (String) adminRoleField.get(null);
            org.junit.jupiter.api.Assertions.assertEquals("ADMIN", adminRole);

            java.lang.reflect.Field actuatorEndpointsField = SecurityConfig.class.getDeclaredField("ACTUATOR_ENDPOINTS");
            actuatorEndpointsField.setAccessible(true);
            String actuatorEndpoints = (String) actuatorEndpointsField.get(null);
            org.junit.jupiter.api.Assertions.assertEquals("/actuator/**", actuatorEndpoints);

            java.lang.reflect.Field swaggerUiField = SecurityConfig.class.getDeclaredField("SWAGGER_UI");
            swaggerUiField.setAccessible(true);
            String swaggerUi = (String) swaggerUiField.get(null);
            org.junit.jupiter.api.Assertions.assertEquals("/swagger-ui/**", swaggerUi);

            java.lang.reflect.Field apiDocsField = SecurityConfig.class.getDeclaredField("API_DOCS");
            apiDocsField.setAccessible(true);
            String apiDocs = (String) apiDocsField.get(null);
            org.junit.jupiter.api.Assertions.assertEquals("/api-docs/**", apiDocs);

            java.lang.reflect.Field postUsersField = SecurityConfig.class.getDeclaredField("POST_USERS");
            postUsersField.setAccessible(true);
            String postUsers = (String) postUsersField.get(null);
            org.junit.jupiter.api.Assertions.assertEquals("/users", postUsers);

        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Failed to access SecurityConfig constants: " + e.getMessage());
        }
    }
}
