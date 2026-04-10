package com.eerdem07.mira.gateway.merchants.application;

import com.eerdem07.mira.gateway.merchants.application.port.in.GenerateApiKeyCommand;
import com.eerdem07.mira.gateway.merchants.application.port.in.GenerateApiKeyResult;
import com.eerdem07.mira.gateway.merchants.application.port.in.GenerateApiKeyUseCase;
import com.eerdem07.mira.gateway.merchants.application.port.out.ApiCredentialRepositoryPort;
import com.eerdem07.mira.gateway.merchants.domain.ApiCredential;
import com.eerdem07.mira.gateway.merchants.domain.ApiCredentialEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private static final int SECRET_PREFIX_LENGTH = 6; // ilk 6 karakter

    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    private final ApiCredentialRepositoryPort apiCredentialRepository;

    public GenerateApiKeyService(PasswordEncoder passwordEncoder, Clock clock, ApiCredentialRepositoryPort apiCredentialRepository) {
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.apiCredentialRepository = apiCredentialRepository;
    }


    @Override
    public GenerateApiKeyResult execute(GenerateApiKeyCommand command) {
        UUID credentialId = UUID.randomUUID();
        UUID merchantId = command.merchantId();

        String keyId = generateKeyId();
        String plainSecret = generateSecret();
        String secretHash = passwordEncoder.encode(plainSecret);
        String secretPrefix = plainSecret.substring(0, SECRET_PREFIX_LENGTH);
        Instant now = Instant.now(clock);

        ApiCredential apiCredential = ApiCredential.create(
                credentialId,
                merchantId,
                ApiCredentialEnvironment.LIVE,
                keyId,
                secretHash,
                secretPrefix,
                now
        );

        this.apiCredentialRepository.save(apiCredential);

        return new GenerateApiKeyResult("asd");
    }

    // PRIVATE HELPER METHODS

    private String generateKeyId() {
        byte[] bytes = new byte[KEY_ID_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String generateSecret() {
        byte[] bytes = new byte[SECRET_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
