package com.eerdem07.mira.gateway.merchants.persistence;

import com.eerdem07.mira.gateway.merchants.domain.MerchantStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "merchants")
public class MerchantJpaEntity {

    @Id
    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Email
    @Column(name = "email", nullable = false, length = 320, unique = true)
    private String email;

    @JsonIgnore // API’ye sızmasın
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "legal_name", nullable = false, length = 200)
    private String legalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MerchantStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "suspended_at")
    private Instant suspendedAt;

    public MerchantJpaEntity(
            UUID merchantId,
            String email,
            String passwordHash,
            String legalName,
            MerchantStatus status,
            Instant createdAt,
            Instant activatedAt,
            Instant suspendedAt
    ) {
        this.merchantId = merchantId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.legalName = legalName;
        this.status = status;
        this.createdAt = createdAt;
        this.activatedAt = activatedAt;
        this.suspendedAt = suspendedAt;
    }

}
