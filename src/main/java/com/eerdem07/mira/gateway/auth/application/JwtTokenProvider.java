package com.eerdem07.mira.gateway.auth.application;

import com.eerdem07.mira.gateway.auth.application.port.in.GenerateAccessTokenCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.access-token-minutes}")
    private Duration accessTokenMinutes;

    public JwtTokenProvider(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(GenerateAccessTokenCommand command) {
        Instant now = Instant.now();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(command.merchantId()
                        .toString())
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenMinutes))
                .id(UUID.randomUUID()
                        .toString())
                .claim("merchant_id", command.merchantId()
                        .toString())
                .build();


        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();

    }
}
