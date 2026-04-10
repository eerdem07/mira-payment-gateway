package com.eerdem07.mira.gateway.merchants.persistence;


import com.eerdem07.mira.gateway.merchants.domain.ApiCredentialEnvironment;
import com.eerdem07.mira.gateway.merchants.domain.ApiCredentialStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "api_credential",
        indexes = {
                @Index(name = "idx_api_credential_key_id", columnList = "key_id", unique = true),
                @Index(name = "idx_api_credential_merchant_id", columnList = "merchant_id") // Merchant'ın key'lerini listelemek için
        }
)
public class ApiCredentialJpaEntity {
    @Id
    @Column(name = "credential_id", nullable = false, updatable = false)
    private UUID credentialId;

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "key_id", nullable = false, updatable = false, unique = true)
    private String keyId;

    @Column(name = "secret_hash", nullable = false, updatable = false)
    private String secretHash;

    @Column(name = "secret_prefix")
    private String secretPrefix;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false, length = 20)
    private ApiCredentialEnvironment environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ApiCredentialStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    // Concurrency için bunu koyduk. Hibernate benim adıma yönetiyor.
    // OPTIMISTIC LOCKING
    // toEntity ve toDomain işlemi yapıp, güncellemeyi domain üzerinden yapıyorsam
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public ApiCredentialJpaEntity(UUID credentialId,
                                  UUID merchantId,
                                  String keyId,
                                  String secretHash,
                                  String secretPrefix,
                                  ApiCredentialEnvironment environment,
                                  ApiCredentialStatus status,
                                  Instant createdAt,
                                  Instant lastUsedAt,
                                  Instant revokedAt) {
        this.credentialId = credentialId;
        this.merchantId = merchantId;
        this.keyId = keyId;
        this.secretHash = secretHash;
        this.secretPrefix = secretPrefix;
        this.environment = environment;
        this.status = status;
        this.createdAt = createdAt;
        this.lastUsedAt = lastUsedAt;
        this.revokedAt = revokedAt;
    }


}
