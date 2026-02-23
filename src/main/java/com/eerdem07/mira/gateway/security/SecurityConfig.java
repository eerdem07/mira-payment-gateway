package com.eerdem07.mira.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Geliştirme aşamasında Postman veya Swagger'dan POST/PUT/DELETE
                // istekleri atarken 403 Forbidden hatası almamak için CSRF'yi kapatıyoruz.
                .csrf(csrf -> csrf.disable())

                // Tüm isteklere (herhangi bir URL'e ve herhangi bir HTTP metoduna) izin veriyoruz.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest()
                        .permitAll()
                );

        return http.build();
    }
}