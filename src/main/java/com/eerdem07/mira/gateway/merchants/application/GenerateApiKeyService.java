package com.eerdem07.mira.gateway.merchants.application;

import com.eerdem07.mira.gateway.merchants.application.port.in.GenerateApiKeyCommand;
import com.eerdem07.mira.gateway.merchants.application.port.in.GenerateApiKeyResult;
import com.eerdem07.mira.gateway.merchants.application.port.in.GenerateApiKeyUseCase;
import com.eerdem07.mira.gateway.merchants.application.port.out.ApiCredentialRepositoryPort;
import com.eerdem07.mira.gateway.merchants.domain.ApiCredential;
import com.eerdem07.mira.gateway.merchants.domain.ApiCredentialEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class GenerateApiKeyService implements GenerateApiKeyUseCase {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int KEY_ID_BYTE_LENGTH = 16; // 128-bit
    private static final int SECRET_BYTE_LENGTH = 32; // 256-bit

    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final ApiCredentialRepositoryPort apiCredentialRepository;

    public GenerateApiKeyService(PasswordEncoder passwordEncoder, Clock clock, ApiCredentialRepositoryPort apiCredentialRepository) {
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.apiCredentialRepository = apiCredentialRepository;
    }

    @Transactional
    @Override
    public GenerateApiKeyResult execute(GenerateApiKeyCommand command) {
        UUID credentialId = UUID.randomUUID();
        UUID merchantId = command.merchantId();
        ApiCredentialEnvironment env = ApiCredentialEnvironment.LIVE;

        String randomKeyId = generateRandomBase64(KEY_ID_BYTE_LENGTH);
        String randomSecret = generateRandomBase64(SECRET_BYTE_LENGTH);

        String keyId = ApiCredential.formatKeyId(env, randomKeyId);
        String plainSecret = ApiCredential.formatSecret(env, randomSecret);

        String secretHash = passwordEncoder.encode(plainSecret);
        String secretSuffix = ApiCredential.extractSecretSuffix(plainSecret);
        Instant now = Instant.now(clock);

        ApiCredential apiCredential = ApiCredential.create(
                credentialId,
                merchantId,
                env,
                keyId,
                secretHash,
                secretSuffix,
                now
        );

        this.apiCredentialRepository.save(apiCredential);

        String basicAuthToken = Base64.getEncoder().encodeToString(
                (keyId + ":" + plainSecret).getBytes(StandardCharsets.UTF_8)
        );

        return new GenerateApiKeyResult(keyId, plainSecret, basicAuthToken);
    }

    private String generateRandomBase64(int byteLength) {
        byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}