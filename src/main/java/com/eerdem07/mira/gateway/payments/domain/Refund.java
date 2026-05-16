package com.eerdem07.mira.gateway.payments.domain;

import com.eerdem07.mira.gateway.payments.domain.exception.InvalidRefundAmountException;
import com.eerdem07.mira.gateway.payments.domain.exception.InvalidRefundStateTransitionException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Refund {

    private final UUID id;
    private final UUID paymentIntentId;
    private final UUID paymentAttemptId;

    private RefundStatus status;

    private final BigDecimal amount;
    private final Currency currency;

    private final String posProvider;

    private String posRefundId;
    private String posReference;
    private String posResponseCode;
    private String posResponseMessage;

    private String failureCode;
    private String failureMessage;

    private final Instant createdAt;
    private Instant updatedAt;
    private Instant processingStartedAt;
    private Instant succeededAt;
    private Instant failedAt;
    private Instant canceledAt;

    private Refund(
            UUID id,
            UUID paymentIntentId,
            UUID paymentAttemptId,
            RefundStatus status,
            BigDecimal amount,
            Currency currency,
            String posProvider,
            String posRefundId,
            String posReference,
            String posResponseCode,
            String posResponseMessage,
            String failureCode,
            String failureMessage,
            Instant createdAt,
            Instant updatedAt,
            Instant processingStartedAt,
            Instant succeededAt,
            Instant failedAt,
            Instant canceledAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.paymentIntentId = Objects.requireNonNull(paymentIntentId, "paymentIntentId must not be null");
        this.paymentAttemptId = Objects.requireNonNull(paymentAttemptId, "paymentAttemptId must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.amount = validateAmount(amount);
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
        this.posProvider = requireText(posProvider, "posProvider must not be blank");
        this.posRefundId = posRefundId;
        this.posReference = posReference;
        this.posResponseCode = posResponseCode;
        this.posResponseMessage = posResponseMessage;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        this.processingStartedAt = processingStartedAt;
        this.succeededAt = succeededAt;
        this.failedAt = failedAt;
        this.canceledAt = canceledAt;
    }

    public static Refund create(
            UUID id,
            UUID paymentIntentId,
            UUID paymentAttemptId,
            BigDecimal amount,
            Currency currency,
            String posProvider,
            Instant createdAt
    ) {
        return new Refund(
                id,
                paymentIntentId,
                paymentAttemptId,
                RefundStatus.CREATED,
                amount,
                currency,
                posProvider,
                null,
                null,
                null,
                null,
                null,
                null,
                createdAt,
                createdAt,
                null,
                null,
                null,
                null
        );
    }

    public static Refund restore(
            UUID id,
            UUID paymentIntentId,
            UUID paymentAttemptId,
            RefundStatus status,
            BigDecimal amount,
            Currency currency,
            String posProvider,
            String posRefundId,
            String posReference,
            String posResponseCode,
            String posResponseMessage,
            String failureCode,
            String failureMessage,
            Instant createdAt,
            Instant updatedAt,
            Instant processingStartedAt,
            Instant succeededAt,
            Instant failedAt,
            Instant canceledAt
    ) {
        return new Refund(
                id,
                paymentIntentId,
                paymentAttemptId,
                status,
                amount,
                currency,
                posProvider,
                posRefundId,
                posReference,
                posResponseCode,
                posResponseMessage,
                failureCode,
                failureMessage,
                createdAt,
                updatedAt,
                processingStartedAt,
                succeededAt,
                failedAt,
                canceledAt
        );
    }

    public void markProcessing(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        ensureStatusIn(RefundStatus.PROCESSING, RefundStatus.CREATED);

        this.status = RefundStatus.PROCESSING;
        this.processingStartedAt = now;
        this.updatedAt = now;
    }

    public void markSucceeded(
            String posRefundId,
            String posReference,
            String posResponseCode,
            String posResponseMessage,
            Instant now
    ) {
        Objects.requireNonNull(now, "now must not be null");
        ensureStatusIn(RefundStatus.SUCCEEDED, RefundStatus.PROCESSING, RefundStatus.PENDING);

        this.status = RefundStatus.SUCCEEDED;
        this.posRefundId = requireText(posRefundId, "posRefundId must not be blank");
        this.posReference = posReference;
        this.posResponseCode = requireText(posResponseCode, "posResponseCode must not be blank");
        this.posResponseMessage = posResponseMessage;
        this.succeededAt = now;
        this.updatedAt = now;
    }

    public void markFailed(
            String failureCode,
            String failureMessage,
            String posResponseCode,
            String posResponseMessage,
            Instant now
    ) {
        Objects.requireNonNull(now, "now must not be null");
        ensureStatusIn(RefundStatus.FAILED, RefundStatus.PROCESSING, RefundStatus.PENDING);

        this.status = RefundStatus.FAILED;
        this.failureCode = requireText(failureCode, "failureCode must not be blank");
        this.failureMessage = failureMessage;
        this.posResponseCode = posResponseCode;
        this.posResponseMessage = posResponseMessage;
        this.failedAt = now;
        this.updatedAt = now;
    }

    public void cancel(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        ensureStatusIn(RefundStatus.CANCELED, RefundStatus.CREATED, RefundStatus.PROCESSING);

        this.status = RefundStatus.CANCELED;
        this.canceledAt = now;
        this.updatedAt = now;
    }

    private void ensureStatusIn(RefundStatus targetStatus, RefundStatus... allowedStatuses) {
        for (RefundStatus allowedStatus : allowedStatuses) {
            if (this.status == allowedStatus) {
                return;
            }
        }

        throw new InvalidRefundStateTransitionException(this.status, targetStatus);
    }

    private static BigDecimal validateAmount(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount must not be null");

        if (amount.signum() <= 0) {
            throw new InvalidRefundAmountException();
        }

        return amount;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }
}
