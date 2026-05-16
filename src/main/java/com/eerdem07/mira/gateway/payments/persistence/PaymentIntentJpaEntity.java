package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.domain.CaptureMethod;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payment_intents")
public class PaymentIntentJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "merchant_id", nullable = false, updatable = false)
    private UUID merchantId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "capture_method", nullable = false, length = 20)
    private CaptureMethod captureMethod;

    @Column(name = "merchant_reference")
    private String merchantReference;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PaymentIntentStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "failure_code", length = 100)
    private String failureCode;

    @Column(name = "failure_message", length = 1000)
    private String failureMessage;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "authorization_expires_at")
    private Instant authorizationExpiresAt;

    @Column(name = "authorized_payment_attempt_id")
    private UUID authorizedPaymentAttemptId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "succeeded_at")
    private Instant succeededAt;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    public PaymentIntentJpaEntity(
            UUID id,
            UUID merchantId,
            BigDecimal amount,
            Currency currency,
            CaptureMethod captureMethod,
            String merchantReference,
            String description,
            PaymentIntentStatus status,
            int attemptCount,
            String failureCode,
            String failureMessage,
            Instant expiresAt,
            Instant authorizationExpiresAt,
            UUID authorizedPaymentAttemptId,
            Instant createdAt,
            Instant updatedAt,
            Instant succeededAt,
            Instant canceledAt
    ) {
        this.id = id;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.captureMethod = captureMethod;
        this.merchantReference = merchantReference;
        this.description = description;
        this.status = status;
        this.attemptCount = attemptCount;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.expiresAt = expiresAt;
        this.authorizationExpiresAt = authorizationExpiresAt;
        this.authorizedPaymentAttemptId = authorizedPaymentAttemptId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.succeededAt = succeededAt;
        this.canceledAt = canceledAt;
    }

}
