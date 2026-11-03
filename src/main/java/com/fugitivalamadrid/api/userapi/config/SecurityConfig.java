package com.fugitivalamadrid.api.userapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Production security configuration: stateless JWT resource server backed by Keycloak.
 */
@Configuration
@EnableWebSecurity
@Profile("!test")
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private static final String USERS_ENDPOINT = "/users/**";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String ACTUATOR_ENDPOINTS = "/actuator/**";
    private static final String SWAGGER_UI = "/swagger-ui/**";
    private static final String API_DOCS = "/api-docs/**";
    private static final String POST_USERS = "/users";

    private static final String ISSUER_URI_PROPERTY = "spring.security.oauth2.resourceserver.jwt.issuer-uri";
    private static final String JWK_SET_PATH = "/protocol/openid-connect/certs";
    private static final String DEFAULT_ISSUER_URI = "http://localhost:8180/realms/userapi-realm";
    private static final String DEFAULT_TEST_ISSUER_URI = "http://keycloak:8080/realms/userapi-realm";

    private final JwtAuthConverter jwtAuthConverter;

    /**
     * Constructor for SecurityConfig.
     *
     * @param jwtAuthConverter the converter that maps JWT * Not loaded under the "test" profile, where a separate test security configuration is used instead. claims to Spring authorities
     */
    public SecurityConfig(JwtAuthConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }

    /**
     * Configures HTTP security: stateless sessions, public docs/actuator endpoints,
     * ADMIN-only access to /users, and JWT validation.
     *
     * @param http the HttpSecurity builder
     * @return the security filter chain
     * @throws Exception if the configuration cannot be built
     */
    @Bean
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("THROWS_METHOD_THROWS_CLAUSE_BASIC_EXCEPTION")
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
                        // Read and write access to users — admin only
                        .requestMatchers(HttpMethod.GET, USERS_ENDPOINT).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.POST, POST_USERS).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PUT, USERS_ENDPOINT).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PATCH, USERS_ENDPOINT).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, USERS_ENDPOINT).hasRole(ADMIN_ROLE)
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                );

        return http.build();
    }

    /**
     * Returns a JwtDecoder that validates tokens against the configured issuer.
     * If no issuer is configured, falls back to the local Keycloak realm's JWK set
     * (no issuer validation, no network call at startup).
     *
     * @param env the Spring environment
     * @return a JwtDecoder bean
     */
    @Bean
    public JwtDecoder jwtDecoder(Environment env) {
        String issuerUri = env.getProperty(ISSUER_URI_PROPERTY);

        if (issuerUri == null || issuerUri.isBlank()) {
            log.warn("No issuer-uri configured, falling back to JWK set of {}", DEFAULT_ISSUER_URI);
            return NimbusJwtDecoder.withJwkSetUri(DEFAULT_ISSUER_URI + JWK_SET_PATH).build();
        }

        log.info("Configuring JwtDecoder with issuer location: {}", issuerUri);
        return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    }

    /**
     * Creates a JwtDecoder that doesn't require network connectivity at construction time.
     * Used by unit tests to verify decoder creation without connecting to Keycloak.
     *
     * @param issuerUri the issuer URI to use; a default is applied if null or empty
     * @return a JwtDecoder for testing
     */
    public JwtDecoder createTestJwtDecoder(String issuerUri) {
        String effectiveIssuer = (issuerUri == null || issuerUri.isEmpty())
                ? DEFAULT_TEST_ISSUER_URI
                : issuerUri;
        return NimbusJwtDecoder.withJwkSetUri(effectiveIssuer + JWK_SET_PATH).build();
    }
}