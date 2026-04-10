package com.eerdem07.mira.gateway.merchants.domain;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ApiCredential {
    private final UUID credentialId;
    private final UUID merchantId;
    private final String keyId;
    private final String secretHash;
    private final String secretPrefix;
    private final Instant createdAt;
    private ApiCredentialEnvironment apiCredentialEnvironment;
    private ApiCredentialStatus apiCredentialStatus;
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
        this.apiCredentialStatus = apiCredentialStatus;
        this.keyId = keyId;
        this.secretHash = secretHash;
        this.secretPrefix = secretPrefix;
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

    public static ApiCredential restore(UUID credentialId,
                                        UUID merchantId,
                                        ApiCredentialEnvironment environment,
                                        ApiCredentialStatus apiCredentialStatus,
                                        String keyId,
                                        String secretHash,
                                        String secretPrefix,
                                        Instant createdAt,
                                        Instant lastUsedAt,
                                        Instant revokedAt) {
        return new ApiCredential(credentialId, merchantId, environment, keyId, secretHash, secretPrefix, apiCredentialStatus, createdAt, lastUsedAt, revokedAt);
    }
}
