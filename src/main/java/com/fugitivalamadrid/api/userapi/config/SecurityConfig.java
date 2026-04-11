package com.fugitivalamadrid.api.userapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String USERS_ENDPOINT = "/users/**";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String ACTUATOR_HEALTH = "/actuator/health";
    private static final String SWAGGER_UI = "/swagger-ui/**";
    private static final String API_DOCS = "/api-docs/**";
    private static final String REALM_NAME = "userapi";
    private static final String POST_USERS = "/users";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints — no auth needed
                        .requestMatchers(ACTUATOR_HEALTH).permitAll()
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
                .httpBasic(basic -> basic.realmName(REALM_NAME));

        return http.build();
    }
}