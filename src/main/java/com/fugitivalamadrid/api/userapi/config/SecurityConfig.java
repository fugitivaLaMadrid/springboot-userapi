package com.fugitivalamadrid.api.userapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String USERS_ENDPOINT = "/users/**";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String ACTUATOR_ENDPOINTS = "/actuator/**";
    private static final String SWAGGER_UI = "/swagger-ui/**";
    private static final String API_DOCS = "/api-docs/**";
    private static final String POST_USERS = "/users";
    private final JwtAuthConverter jwtAuthConverter;

    /**
     * Constructor for SecurityConfig.
     * @param jwtAuthConverter the JwtAuthConverter to use for authentication
     */
    public SecurityConfig(JwtAuthConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints — no auth needed
                        .requestMatchers(ACTUATOR_ENDPOINTS).permitAll()
                        .requestMatchers(SWAGGER_UI).permitAll()
                        .requestMatchers(API_DOCS).permitAll()
                        // GET endpoints — read access
                        .requestMatchers(HttpMethod.GET, USERS_ENDPOINT).hasRole(ADMIN_ROLE)
                        // Write endpoints — admin only
                        .requestMatchers(HttpMethod.POST, POST_USERS).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PUT, USERS_ENDPOINT).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PATCH, USERS_ENDPOINT).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, USERS_ENDPOINT).hasRole(ADMIN_ROLE)
                        // Everything else requires auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthConverter)
                        )
                );

        return http.build();
    }

    /**
     * Returns a JwtDecoder bean that decodes JWTs from the issuer.
     * @param env the Spring environment
     * @return a JwtDecoder bean
     */
    @Bean
    public JwtDecoder jwtDecoder(Environment env) {
        String issuerUri = env.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
        // Check if running in test profile
        boolean isTestProfile = false;
        String[] activeProfiles = env.getActiveProfiles();
        if (activeProfiles != null) {
            isTestProfile = java.util.Arrays.stream(activeProfiles)
                    .anyMatch(profile -> profile.equals("test"));
        }
        
        if (issuerUri == null || issuerUri.isEmpty()) {
            // For tests or when no issuer is configured, use JWK set URI to avoid network calls
            issuerUri = "http://localhost:8180/realms/userapi-realm";
            return NimbusJwtDecoder.withJwkSetUri(issuerUri + "/protocol/openid-connect/certs").build();
        }
        
        if (isTestProfile) {
            // For test profile with configured issuer, use JWK set URI to avoid network calls
            return NimbusJwtDecoder.withJwkSetUri(issuerUri + "/protocol/openid-connect/certs").build();
        }
        // For production, use issuer location to enforce issuer validation
        return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    }

    /**
     * Creates a JwtDecoder for testing purposes that doesn't require network connectivity.
     * This method is used by unit tests to verify JWT decoder creation without connecting to Keycloak.
     * @param issuerUri the issuer URI to use
     * @return a JwtDecoder bean for testing
     */
    public JwtDecoder createTestJwtDecoder(String issuerUri) {
        if (issuerUri == null || issuerUri.isEmpty()) {
            issuerUri = "http://localhost:8180/realms/userapi-realm";
        }
        return NimbusJwtDecoder.withJwkSetUri(issuerUri + "/protocol/openid-connect/certs").build();
    }
}
