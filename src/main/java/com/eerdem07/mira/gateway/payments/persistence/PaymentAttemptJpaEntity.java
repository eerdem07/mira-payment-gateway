package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.domain.PaymentAttemptStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "payment_attempts")
public class PaymentAttemptJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "payment_intent_id", nullable = false, updatable = false)
    private UUID paymentIntentId;

    @Column(name = "checkout_session_id", nullable = false, updatable = false)
    private UUID checkoutSessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PaymentAttemptStatus status;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "card_brand", nullable = false, length = 32)
    private String cardBrand;

    @Column(name = "card_last4", nullable = false, length = 4)
    private String cardLast4;

    @Column(name = "pos_provider", length = 64)
    private String posProvider;

    @Column(name = "pos_transaction_id", length = 128)
    private String posTransactionId;

    @Column(name = "pos_auth_code", length = 64)
    private String posAuthCode;

    @Column(name = "pos_reference", length = 128)
    private String posReference;

    @Column(name = "pos_response_code", length = 32)
    private String posResponseCode;

    @Column(name = "pos_response_message", length = 255)
    private String posResponseMessage;

    @Column(name = "decline_code", length = 64)
    private String declineCode;

    @Column(name = "decline_message", length = 255)
    private String declineMessage;

    @Column(name = "failure_code", length = 64)
    private String failureCode;

    @Column(name = "failure_message", length = 255)
    private String failureMessage;

    @Column(name = "three_ds_session_id", length = 128)
    private String threeDsSessionId;

    @Column(name = "three_ds_flow", length = 32)
    private String threeDsFlow;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "processing_started_at")
    private Instant processingStartedAt;

    @Column(name = "action_required_at")
    private Instant actionRequiredAt;

    @Column(name = "authorized_at")
    private Instant authorizedAt;

    @Column(name = "authorization_expires_at")
    private Instant authorizationExpiresAt;

    @Column(name = "succeeded_at")
    private Instant succeededAt;

    @Column(name = "declined_at")
    private Instant declinedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    @Column(name = "voided_at")
    private Instant voidedAt;

    @Column(name = "expired_at")
    private Instant expiredAt;
}
