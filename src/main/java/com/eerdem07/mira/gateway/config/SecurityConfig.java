package com.eerdem07.mira.gateway.config;

import com.eerdem07.mira.gateway.auth.rest.ApiKeyAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain apiKeySecurityFilterChain(HttpSecurity http, com.eerdem07.mira.gateway.auth.application.ApiKeyAuthenticationService apiKeyAuthenticationService) throws Exception {
        ApiKeyAuthenticationFilter apiKeyFilter = new ApiKeyAuthenticationFilter(apiKeyAuthenticationService);
        return http
                .securityMatcher("/v1/payment-intents", "/v1/payment-intents/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/auth/login").permitAll()
                        .requestMatchers("/v1/merchants").permitAll()
                        .requestMatchers("/v1/checkout-sessions/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                }))
                .build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // Allow all origins for dev/test
        configuration.addAllowedMethod("*"); // Allow all HTTP methods
        configuration.addAllowedHeader("*"); // Allow all headers
        configuration.setAllowCredentials(false);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    JwtEncoder jwtEncoder(@Value("${security.jwt.secret}") String base64Secret) {
        byte[] secret = Base64.getDecoder()
                .decode(base64Secret);
        SecretKey key = new SecretKeySpec(secret, "HmacSHA256");
        return NimbusJwtEncoder.withSecretKey(key)
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String base64Secret) {
        byte[] secret = Base64.getDecoder()
                .decode(base64Secret);
        SecretKey key = new SecretKeySpec(secret, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
