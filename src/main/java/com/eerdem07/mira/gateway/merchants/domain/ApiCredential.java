package com.eerdem07.mira.gateway.merchants.domain;

import java.time.Instant;
import java.util.UUID;

public class ApiCredential {
    private final UUID credentialId;
    private final UUID merchantId;
    private ApiCredentialEnvironment apiCredentialEnvironment;
    private String keyId;
    private String secretHash;
    private String secretPrefix;
    private ApiCredentialStatus apiCredentialStatus;
    private Instant createdAt;
    private Instant revokedAt;
    private Instant lastUsedAt;

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

    public static ApiCredential create(UUID merchantId, ApiCredentialEnvironment apiCredentialEnvironment, String keyId, String secretHash, String secretPrefix) {
        return new ApiCredential(UUID.randomUUID(), merchantId, apiCredentialEnvironment, null, null, null, null, Instant.now(), Instant.now(), null);

    }
}
