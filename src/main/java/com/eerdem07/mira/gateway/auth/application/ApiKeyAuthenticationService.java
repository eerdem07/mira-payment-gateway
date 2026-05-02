package com.eerdem07.mira.gateway.auth.application;

import com.eerdem07.mira.gateway.merchants.application.port.out.ApiCredentialRepositoryPort;
import com.eerdem07.mira.gateway.merchants.domain.ApiCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
public class ApiKeyAuthenticationService {

    private final ApiCredentialRepositoryPort apiCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public ApiKeyAuthenticationService(ApiCredentialRepositoryPort apiCredentialRepository,
                                       PasswordEncoder passwordEncoder,
                                       Clock clock) {
        this.apiCredentialRepository = apiCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional
    public Optional<ApiCredential> authenticate(String keyId, String plainSecret) {
        Optional<ApiCredential> credentialOpt = apiCredentialRepository.findByKeyId(keyId);
        
        if (credentialOpt.isEmpty()) {
            log.warn("API Key Authentication failed: Key ID '{}' not found in database.", keyId);
            return Optional.empty();
        }

        ApiCredential credential = credentialOpt.get();

        // 1. Secret eşleşiyor mu kontrol et
        if (!passwordEncoder.matches(plainSecret, credential.getSecretHash())) {
            log.warn("API Key Authentication failed: Secret does not match for Key ID '{}'. plainSecretLength={}, DBHashLength={}, plainSecretEndsWith='{}'", 
                     keyId, 
                     plainSecret != null ? plainSecret.length() : 0, 
                     credential.getSecretHash() != null ? credential.getSecretHash().length() : 0,
                     plainSecret != null && plainSecret.length() > 2 ? plainSecret.substring(plainSecret.length() - 2) : "null");
            return Optional.empty();
        }

        // 2. Domain üzerinden durum kontrolü (Aktif mi? İptal edilmiş mi?)
        if (!credential.isEligibleForAuthentication()) {
            log.warn("API Key Authentication failed: Key ID '{}' is not eligible for authentication (Status: {}, RevokedAt: {}).", 
                     keyId, credential.getApiCredentialStatus(), credential.getRevokedAt());
            return Optional.empty();
        }

        // 3. Kullanım tarihini güncelle ve kaydet
        credential.recordUsage(Instant.now(clock));
        apiCredentialRepository.save(credential);
        
        log.info("API Key Authentication successful for Key ID '{}'", keyId);

        return Optional.of(credential);
    }
}
