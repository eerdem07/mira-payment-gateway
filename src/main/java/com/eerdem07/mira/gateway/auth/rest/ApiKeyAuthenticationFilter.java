package com.eerdem07.mira.gateway.auth.rest;

import com.eerdem07.mira.gateway.auth.application.ApiKeyAuthenticationService;
import com.eerdem07.mira.gateway.merchants.domain.ApiCredential;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String BASIC_AUTH_PREFIX = "Basic ";
    private final ApiKeyAuthenticationService apiKeyAuthenticationService;

    public ApiKeyAuthenticationFilter(ApiKeyAuthenticationService apiKeyAuthenticationService) {
        this.apiKeyAuthenticationService = apiKeyAuthenticationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BASIC_AUTH_PREFIX)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Missing or invalid Authorization header. Expected format: 'Basic <base64>'");
            return;
        }

        try {
            // 1. Base64 Decode
            String base64Token = authHeader.substring(BASIC_AUTH_PREFIX.length());
            String decodedToken = new String(Base64.getDecoder().decode(base64Token), StandardCharsets.UTF_8);

            // 2. Split keyId and plainSecret
            String[] parts = decodedToken.split(":", 2);
            if (parts.length != 2) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "Malformed Basic Auth token. Expected format: base64(keyId:secret)");
                return;
            }

            String keyId = parts[0].trim();
            String plainSecret = parts[1].trim();

            // 3. FAIL-FAST Validation: Veritabanına gitmeden önce formatı doğrula
            if (!isValidKeyFormat(keyId, plainSecret)) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Invalid API Key format");
                return;
            }

            // 4. Authenticate via Service
            Optional<ApiCredential> credentialOpt = apiKeyAuthenticationService.authenticate(keyId, plainSecret);

            if (credentialOpt.isPresent()) {
                ApiCredential credential = credentialOpt.get();
                // Authenticate and set the Merchant ID as the principal
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        credential.getMerchantId().toString(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_MERCHANT_API"))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Invalid or revoked API Key");
                return;
            }
        } catch (IllegalArgumentException e) {
            // Base64 decoding failed
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "Invalid Base64 format in Authorization header");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidKeyFormat(String keyId, String plainSecret) {
        // keyId 'mk_' ile başlamalı
        if (keyId == null || !keyId.startsWith("mk_")) {
            return false;
        }
        // secret 'ms_' ile başlamalı
        if (plainSecret == null || !plainSecret.startsWith("ms_")) {
            return false;
        }
        return true;
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String title, String detail) throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        response.getWriter().write(String.format("{\"title\": \"%s\", \"status\": %d, \"detail\": \"%s\"}", title, status, detail));
    }
}