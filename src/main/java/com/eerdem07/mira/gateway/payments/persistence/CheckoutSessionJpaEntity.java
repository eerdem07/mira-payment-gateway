package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.domain.CheckoutSessionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "checkout_sessions")
public class CheckoutSessionJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "payment_intent_id", nullable = false, updatable = false)
    private UUID paymentIntentId;

    @Column(name = "token", nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private CheckoutSessionStatus status;

    @Column(name = "return_url", nullable = false)
    private String returnUrl;

    @Column(name = "cancel_url")
    private String cancelUrl;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    public CheckoutSessionJpaEntity(
            UUID id,
            UUID paymentIntentId,
            String token,
            CheckoutSessionStatus status,
            String returnUrl,
            String cancelUrl,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt,
            Instant submittedAt,
            Instant canceledAt
    ) {
        this.id = id;
        this.paymentIntentId = paymentIntentId;
        this.token = token;
        this.status = status;
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.submittedAt = submittedAt;
        this.canceledAt = canceledAt;
    }
}
