package com.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Stateless security configuration.
 *
 * All endpoints are currently open (permitAll) so the API can be tested
 * directly via curl/Postman/the frontend without a login flow, and so
 * anyone cloning this repo can run it and hit every endpoint immediately
 * with zero setup. This is a deliberate, clearly-marked placeholder — not
 * a finished auth story.
 *
 * What IS already wired up and ready for the real auth story:
 * - Passwords are hashed with BCrypt (see the PasswordEncoder bean) and
 *   AuthController already registers/verifies users against it.
 * - SessionCreationPolicy.STATELESS is set, since a REST API should be
 *   authenticated per-request via a token, not a server-side session.
 *
 * To lock this down later: add a JwtAuthenticationFilter that reads the
 * Authorization: Bearer header, validates the token, and populates the
 * SecurityContext, then replace the permitAll() rule below with something
 * like:
 *   .requestMatchers("/api/auth/**").permitAll()
 *   .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
 *   .anyRequest().authenticated()
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // stateless REST API, no browser form submissions
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // open for testing — see class-level note above
            )
            .httpBasic(httpBasic -> httpBasic.disable()) // no browser login popup
            .formLogin(formLogin -> formLogin.disable());

        return http.build();
    }
}
