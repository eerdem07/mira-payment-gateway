package com.eerdem07.mira.gateway.payments.domain;

import com.eerdem07.mira.gateway.payments.domain.exception.PaymentIntentCannotBeCanceledException;
import com.eerdem07.mira.gateway.payments.domain.exception.PaymentIntentCannotCreateCheckoutSessionException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

@Getter
public class PaymentIntent {
    private final UUID id;
    private final UUID merchantId;
    private final BigDecimal amount;
    private final Currency currency;
    private final CaptureMethod captureMethod;
    private final String merchantReference;
    private final String description;
    private PaymentIntentStatus status;
    private int attemptCount;
    private String failureCode;
    private String failureMessage;
    private Instant expiresAt;
    private Instant authorizationExpiresAt;
    private UUID authorizedPaymentAttemptId;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant succeededAt;
    private Instant canceledAt;

    private PaymentIntent(UUID id, UUID merchantId, BigDecimal amount, Currency currency, CaptureMethod captureMethod,
                          String merchantReference, String description, PaymentIntentStatus status,
                          int attemptCount, String failureCode, String failureMessage,
                          Instant expiresAt, Instant authorizationExpiresAt, UUID authorizedPaymentAttemptId, Instant createdAt,
                          Instant updatedAt, Instant succeededAt, Instant canceledAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.merchantId = Objects.requireNonNull(merchantId, "merchantId");
        this.amount = Objects.requireNonNull(amount, "amount");
        this.currency = Objects.requireNonNull(currency, "currency");
        this.captureMethod = Objects.requireNonNull(captureMethod, "captureMethod");
        this.merchantReference = merchantReference;
        this.description = description;
        this.status = Objects.requireNonNull(status, "status");
        this.attemptCount = attemptCount;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.expiresAt = expiresAt;
        this.authorizationExpiresAt = authorizationExpiresAt;
        this.authorizedPaymentAttemptId = authorizedPaymentAttemptId;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = updatedAt;
        this.succeededAt = succeededAt;
        this.canceledAt = canceledAt;
    }

    public static PaymentIntent create(UUID id, UUID merchantId, BigDecimal amount, Currency currency, CaptureMethod captureMethod,
                                       String merchantReference, String description, Instant now, Instant expiresAt) {
        return new PaymentIntent(
                id,
                merchantId,
                amount,
                currency,
                captureMethod,
                merchantReference,
                description,
                PaymentIntentStatus.REQUIRES_PAYMENT_METHOD,
                0,
                null,
                null,
                expiresAt,
                null,
                null,
                Objects.requireNonNull(now, "now"),
                null,
                null,
                null
        );
    }

    public static PaymentIntent restore(UUID id, UUID merchantId, BigDecimal amount, Currency currency,
                                        String merchantReference, String description, PaymentIntentStatus status,
                                        int attemptCount, String failureCode, String failureMessage,
                                        Instant expiresAt, Instant createdAt, Instant updatedAt,
                                        Instant succeededAt, Instant canceledAt) {
        return restore(
                id,
                merchantId,
                amount,
                currency,
                CaptureMethod.AUTOMATIC,
                merchantReference,
                description,
                status,
                attemptCount,
                failureCode,
                failureMessage,
                expiresAt,
                null,
                null,
                createdAt,
                updatedAt,
                succeededAt,
                canceledAt
        );
    }

    public static PaymentIntent restore(UUID id, UUID merchantId, BigDecimal amount, Currency currency, CaptureMethod captureMethod,
                                        String merchantReference, String description, PaymentIntentStatus status,
                                        int attemptCount, String failureCode, String failureMessage,
                                        Instant expiresAt, Instant authorizationExpiresAt, Instant createdAt,
                                        Instant updatedAt, Instant succeededAt, Instant canceledAt) {
        return restore(
                id,
                merchantId,
                amount,
                currency,
                captureMethod,
                merchantReference,
                description,
                status,
                attemptCount,
                failureCode,
                failureMessage,
                expiresAt,
                authorizationExpiresAt,
                null,
                createdAt,
                updatedAt,
                succeededAt,
                canceledAt
        );
    }

    public static PaymentIntent restore(UUID id, UUID merchantId, BigDecimal amount, Currency currency, CaptureMethod captureMethod,
                                        String merchantReference, String description, PaymentIntentStatus status,
                                        int attemptCount, String failureCode, String failureMessage,
                                        Instant expiresAt, Instant authorizationExpiresAt, UUID authorizedPaymentAttemptId,
                                        Instant createdAt, Instant updatedAt, Instant succeededAt, Instant canceledAt) {
        return new PaymentIntent(
                id, merchantId, amount, currency, captureMethod, merchantReference, description, status,
                attemptCount, failureCode, failureMessage, expiresAt, authorizationExpiresAt, authorizedPaymentAttemptId,
                createdAt, updatedAt, succeededAt, canceledAt
        );
    }

    public void markProcessing(Instant now) {
        ensureStatusIn("be marked processing", PaymentIntentStatus.REQUIRES_PAYMENT_METHOD, PaymentIntentStatus.REQUIRES_ACTION);

        this.status = PaymentIntentStatus.PROCESSING;
        this.failureCode = null;
        this.failureMessage = null;
        this.authorizationExpiresAt = null;
        this.authorizedPaymentAttemptId = null;
        this.updatedAt = Objects.requireNonNull(now, "now");
    }

    public void markRequiresAction(Instant now) {
        ensureStatusIn("require customer action", PaymentIntentStatus.PROCESSING);

        this.status = PaymentIntentStatus.REQUIRES_ACTION;
        this.updatedAt = Objects.requireNonNull(now, "now");
    }

    public void markRequiresPaymentMethod(String failureCode, String failureMessage, Instant now) {
        ensureStatusIn("require a new payment method", PaymentIntentStatus.PROCESSING, PaymentIntentStatus.REQUIRES_ACTION);

        this.status = PaymentIntentStatus.REQUIRES_PAYMENT_METHOD;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.authorizationExpiresAt = null;
        this.authorizedPaymentAttemptId = null;
        this.updatedAt = Objects.requireNonNull(now, "now");
        this.attemptCount++;
    }

    public void markRequiresCapture(UUID authorizedPaymentAttemptId, Instant authorizationExpiresAt, Instant now) {
        Objects.requireNonNull(now, "now");
        Objects.requireNonNull(authorizedPaymentAttemptId, "authorizedPaymentAttemptId");
        Objects.requireNonNull(authorizationExpiresAt, "authorizationExpiresAt");
        ensureStatusIn("require capture", PaymentIntentStatus.PROCESSING, PaymentIntentStatus.REQUIRES_ACTION);

        if (!authorizationExpiresAt.isAfter(now)) {
            throw new IllegalArgumentException("authorizationExpiresAt must be after now");
        }

        this.status = PaymentIntentStatus.REQUIRES_CAPTURE;
        this.failureCode = null;
        this.failureMessage = null;
        this.authorizationExpiresAt = authorizationExpiresAt;
        this.authorizedPaymentAttemptId = authorizedPaymentAttemptId;
        this.updatedAt = now;
    }

    public void markSucceeded(Instant now) {
        ensureStatusIn("succeed", PaymentIntentStatus.PROCESSING, PaymentIntentStatus.REQUIRES_ACTION, PaymentIntentStatus.REQUIRES_CAPTURE);

        this.status = PaymentIntentStatus.SUCCEEDED;
        this.failureCode = null;
        this.failureMessage = null;
        this.authorizationExpiresAt = null;
        this.authorizedPaymentAttemptId = null;
        this.succeededAt = Objects.requireNonNull(now, "now");
        this.updatedAt = now;
    }

    public void markRefunded(Instant now) {
        ensureStatusIn("be refunded", PaymentIntentStatus.SUCCEEDED);

        this.status = PaymentIntentStatus.REFUNDED;
        this.failureCode = null;
        this.failureMessage = null;
        this.updatedAt = Objects.requireNonNull(now, "now");
    }

    public void markFailed(String failureCode, String failureMessage, Instant now) {
        ensureStatusIn("fail", PaymentIntentStatus.PROCESSING, PaymentIntentStatus.REQUIRES_ACTION);

        this.status = PaymentIntentStatus.FAILED;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.authorizationExpiresAt = null;
        this.authorizedPaymentAttemptId = null;
        this.updatedAt = Objects.requireNonNull(now, "now");
        this.attemptCount++;
    }

    public void cancel(Instant now) {
        Objects.requireNonNull(now, "now must not be null");

        if (this.status == PaymentIntentStatus.CANCELED) {
            return;
        }

        if (!isCancellable()) {
            throw new PaymentIntentCannotBeCanceledException(this.status);
        }

        this.status = PaymentIntentStatus.CANCELED;
        if (this.canceledAt == null) {
            this.canceledAt = now;
        }
        this.updatedAt = now;
    }

    public boolean isCancellable() {
        return this.status == PaymentIntentStatus.REQUIRES_PAYMENT_METHOD
                || this.status == PaymentIntentStatus.REQUIRES_ACTION
                || this.status == PaymentIntentStatus.REQUIRES_CAPTURE;
    }

    public void validateCheckoutSessionCreatable() {
        if (this.status != PaymentIntentStatus.REQUIRES_PAYMENT_METHOD) {
            throw new PaymentIntentCannotCreateCheckoutSessionException(this.status);
        }
    }

    public void expire(Instant now) {
        Objects.requireNonNull(now, "now");

        if (!isExpirable()) {
            throw new IllegalStateException("PaymentIntent cannot expire from status " + this.status);
        }

        this.status = PaymentIntentStatus.EXPIRED;
        this.updatedAt = now;
    }

    public boolean isExpirable() {
        return this.status == PaymentIntentStatus.REQUIRES_PAYMENT_METHOD
                || this.status == PaymentIntentStatus.REQUIRES_ACTION
                || this.status == PaymentIntentStatus.REQUIRES_CAPTURE;
    }

    private void ensureStatusIn(String action, PaymentIntentStatus... allowedStatuses) {
        for (PaymentIntentStatus allowedStatus : allowedStatuses) {
            if (this.status == allowedStatus) {
                return;
            }
        }

        throw new IllegalStateException("PaymentIntent cannot " + action + " from status " + this.status);
    }
}
