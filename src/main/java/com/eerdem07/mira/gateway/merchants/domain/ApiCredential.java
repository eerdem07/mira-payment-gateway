package com.eerdem07.mira.gateway.merchants.domain;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class ApiCredential {
    private final UUID credentialId;
    private final UUID merchantId;
    private final String keyId;
    private final String secretHash;
    private final String secretSuffix;
    private final Instant createdAt;
    private ApiCredentialEnvironment apiCredentialEnvironment;
    private ApiCredentialStatus apiCredentialStatus;
    private Instant lastUsedAt;
    private Instant revokedAt;
    
    // For Optimistic Locking
    private Long version;

    public void setVersion(Long version) {
        this.version = version;
    }

    private ApiCredential(UUID credentialId,
                          UUID merchantId,
                          ApiCredentialEnvironment apiCredentialEnvironment,
                          String keyId,
                          String secretHash,
                          String secretSuffix,
                          ApiCredentialStatus apiCredentialStatus,
                          Instant createdAt,
                          Instant revokedAt,
                          Instant lastUsedAt
    ) {
        this.credentialId = Objects.requireNonNull(credentialId, "credentialId");
        this.merchantId = Objects.requireNonNull(merchantId, "merchantId");
        this.apiCredentialEnvironment = Objects.requireNonNull(apiCredentialEnvironment, "apiCredentialEnvironment");
        this.keyId = Objects.requireNonNull(keyId, "keyId");
        this.secretHash = Objects.requireNonNull(secretHash, "secretHash");
        this.secretSuffix = Objects.requireNonNull(secretSuffix, "secretSuffix");
        this.apiCredentialStatus = Objects.requireNonNull(apiCredentialStatus, "apiCredentialStatus");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.revokedAt = revokedAt;
        this.lastUsedAt = lastUsedAt;
    }

    public static ApiCredential create(
            UUID credentialId,
            UUID merchantId,
            ApiCredentialEnvironment environment,
            String keyId,
            String secretHash,
            String secretSuffix,
            Instant createdAt
    ) {
        return new ApiCredential(
                credentialId,
                merchantId,
                environment,
                keyId,
                secretHash,
                secretSuffix,
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
                                        String secretSuffix,
                                        Instant createdAt,
                                        Instant lastUsedAt,
                                        Instant revokedAt) {
        return new ApiCredential(
                credentialId, 
                merchantId, 
                environment, 
                keyId, 
                secretHash, 
                secretSuffix, 
                apiCredentialStatus, 
                createdAt, 
                revokedAt, 
                lastUsedAt
        );
    }

    public static String formatKeyId(ApiCredentialEnvironment environment, String randomBase64Part) {
        return "mk_" + environment.name().toLowerCase(java.util.Locale.ENGLISH) + "_" + randomBase64Part;
    }

    public static String formatSecret(ApiCredentialEnvironment environment, String randomBase64Part) {
        return "ms_" + environment.name().toLowerCase(java.util.Locale.ENGLISH) + "_" + randomBase64Part;
    }

    public static String extractSecretSuffix(String fullSecret) {
        if (fullSecret == null || fullSecret.length() < 4) {
            return fullSecret;
        }
        return fullSecret.substring(fullSecret.length() - 4);
    }

    public boolean isEligibleForAuthentication() {
        return this.apiCredentialStatus == ApiCredentialStatus.ACTIVE && this.revokedAt == null;
    }

    public void recordUsage(Instant usedAt) {
        this.lastUsedAt = usedAt;
    }
}
