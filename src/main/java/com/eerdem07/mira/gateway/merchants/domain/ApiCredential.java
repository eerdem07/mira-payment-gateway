package com.eerdem07.mira.gateway.merchants.domain;

import java.time.Instant;
import java.util.UUID;

public class ApiCredential {
    private final UUID credentialId;
    private final UUID merchantId;
    private ApiCredentialEnvironment apiCredentialEnvironment;
    private String keyId;
    private String secretHash;
    private ApiCredentialStatus apiCredentialStatus;
    private Instant createdAt;
    private Instant revokedAt;
    private Instant lastUsedAt;
    private String secretPrefix;

    private ApiCredential(UUID credentialId,
                          UUID merchantId,
                          ApiCredentialEnvironment apiCredentialEnvironment,
                          String keyId,
                          String secretHash,
                          ApiCredentialStatus apiCredentialStatus,
                          Instant createdAt,
                          Instant revokedAt,
                          Instant lastUsedAt,
                          String secretPrefix
    ) {
        this.credentialId = credentialId;
        this.merchantId = merchantId;
    }
}
