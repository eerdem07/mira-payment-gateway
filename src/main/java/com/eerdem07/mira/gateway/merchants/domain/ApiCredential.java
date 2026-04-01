package com.eerdem07.mira.gateway.merchants.domain;

import java.time.Instant;
import java.util.UUID;

public class ApiCredential {
    private final UUID credentialId;
    private final UUID merchantId;
    private ApiCredentialEnvironment apiCredentialEnvironment;
    private final String keyId;
    private final String secretHash;
    private final String secretPrefix;
    private ApiCredentialStatus apiCredentialStatus;
    private final Instant createdAt;
    private Instant lastUsedAt;
    private Instant revokedAt;

    private ApiCredential(UUID credentialId,
                          UUID merchantId,
                          ApiCredentialEnvironment apiCredentialEnvironment,
                          String keyId,
                          String secretHash,
                          String secretPrefix,
                          ApiCredentialStatus apiCredentialStatus,
                          Instant createdAt,
                          Instant revokedAt,
                          Instant lastUsedAt
    ) {
        this.credentialId = credentialId;
        this.merchantId = merchantId;
        this.apiCredentialEnvironment = apiCredentialEnvironment;
        this.keyId = keyId;
        this.secretHash = secretHash;
        this.secretPrefix = secretPrefix;
        this.apiCredentialStatus = apiCredentialStatus;
        this.createdAt = createdAt;
        this.revokedAt = revokedAt;
        this.lastUsedAt = lastUsedAt;

    }

    public static ApiCredential create(
            UUID credentialId,
            UUID merchantId,
            ApiCredentialEnvironment environment,
            String keyId,
            String secretHash,
            String secretPrefix,
            Instant createdAt
    ) {
        return new ApiCredential(
                credentialId,
                merchantId,
                environment,
                keyId,
                secretHash,
                secretPrefix,
                ApiCredentialStatus.ACTIVE,
                createdAt,
                null,
                null
        );
    }
}
