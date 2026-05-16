package com.eerdem07.mira.gateway.payments.domain;

import com.eerdem07.mira.gateway.payments.domain.exception.InvalidPaymentAttemptAmountException;
import com.eerdem07.mira.gateway.payments.domain.exception.InvalidPaymentAttemptStateTransitionException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

@Getter
public class PaymentAttempt {

    private final UUID id;
    private final UUID paymentIntentId;
    private final UUID checkoutSessionId;

    private PaymentAttemptStatus status;

    private final BigDecimal amount;
    private final Currency currency;

    private final String cardBrand;
    private final String cardLast4;

    private String posProvider;
    private String posTransactionId;
    private String posAuthCode;
    private String posReference;
    private String posResponseCode;
    private String posResponseMessage;

    private String declineCode;
    private String declineMessage;

    private String failureCode;
    private String failureMessage;

    private String threeDsSessionId;
    private String threeDsFlow;

    private final Instant createdAt;
    private Instant updatedAt;
    private Instant processingStartedAt;
    private Instant actionRequiredAt;
    private Instant authorizedAt;
    private Instant authorizationExpiresAt;
    private Instant succeededAt;
    private Instant declinedAt;
    private Instant failedAt;
    private Instant canceledAt;
    private Instant voidedAt;
    private Instant expiredAt;

    private PaymentAttempt(
            UUID id,
            UUID paymentIntentId,
            UUID checkoutSessionId,
            PaymentAttemptStatus status,
            BigDecimal amount,
            Currency currency,
            String cardBrand,
            String cardLast4,
            String posProvider,
            String posTransactionId,
            String posAuthCode,
            String posReference,
            String posResponseCode,
            String posResponseMessage,
            String declineCode,
            String declineMessage,
            String failureCode,
            String failureMessage,
            String threeDsSessionId,
            String threeDsFlow,
            Instant createdAt,
            Instant updatedAt,
            Instant processingStartedAt,
            Instant actionRequiredAt,
            Instant authorizedAt,
            Instant authorizationExpiresAt,
            Instant succeededAt,
            Instant declinedAt,
            Instant failedAt,
            Instant canceledAt,
            Instant voidedAt,
            Instant expiredAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.paymentIntentId = Objects.requireNonNull(paymentIntentId, "paymentIntentId must not be null");
        this.checkoutSessionId = Objects.requireNonNull(checkoutSessionId, "checkoutSessionId must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.amount = validateAmount(amount);
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
        this.cardBrand = requireText(cardBrand, "cardBrand must not be blank");
        this.cardLast4 = validateCardLast4(cardLast4);
        this.posProvider = posProvider;
        this.posTransactionId = posTransactionId;
        this.posAuthCode = posAuthCode;
        this.posReference = posReference;
        this.posResponseCode = posResponseCode;
        this.posResponseMessage = posResponseMessage;
        this.declineCode = declineCode;
        this.declineMessage = declineMessage;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.threeDsSessionId = threeDsSessionId;
        this.threeDsFlow = threeDsFlow;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        this.processingStartedAt = processingStartedAt;
        this.actionRequiredAt = actionRequiredAt;
        this.authorizedAt = authorizedAt;
        this.authorizationExpiresAt = authorizationExpiresAt;
        this.succeededAt = succeededAt;
        this.declinedAt = declinedAt;
        this.failedAt = failedAt;
        this.canceledAt = canceledAt;
        this.voidedAt = voidedAt;
        this.expiredAt = expiredAt;
    }

    public static PaymentAttempt create(
            UUID id,
            UUID paymentIntentId,
            UUID checkoutSessionId,
            BigDecimal amount,
            Currency currency,
            String cardBrand,
            String cardLast4,
            Instant createdAt
    ) {
        return new PaymentAttempt(
                id,
                paymentIntentId,
                checkoutSessionId,
                PaymentAttemptStatus.INITIATED,
                amount,
                currency,
                cardBrand,
                cardLast4,
                null,
                null,
                null,
                null,
                null,
                null,
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
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static PaymentAttempt restore(
            UUID id,
            UUID paymentIntentId,
            UUID checkoutSessionId,
            PaymentAttemptStatus status,
            BigDecimal amount,
            Currency currency,
            String cardBrand,
            String cardLast4,
            String posProvider,
            String posTransactionId,
            String posAuthCode,
            String posReference,
            String posResponseCode,
            String posResponseMessage,
            String declineCode,
            String declineMessage,
            String failureCode,
            String failureMessage,
            Instant createdAt,
            Instant updatedAt,
            Instant processingStartedAt,
            Instant succeededAt,
            Instant declinedAt,
            Instant failedAt
    ) {
        return restore(
                id,
                paymentIntentId,
                checkoutSessionId,
                status,
                amount,
                currency,
                cardBrand,
                cardLast4,
                posProvider,
                posTransactionId,
                posAuthCode,
                posReference,
                posResponseCode,
                posResponseMessage,
                declineCode,
                declineMessage,
                failureCode,
                failureMessage,
                null,
                null,
                createdAt,
                updatedAt,
                processingStartedAt,
                null,
                null,
                null,
                succeededAt,
                declinedAt,
                failedAt,
                null,
                null,
                null
        );
    }

    public static PaymentAttempt restore(
            UUID id,
            UUID paymentIntentId,
            UUID checkoutSessionId,
            PaymentAttemptStatus status,
            BigDecimal amount,
            Currency currency,
            String cardBrand,
            String cardLast4,
            String posProvider,
            String posTransactionId,
            String posAuthCode,
            String posReference,
            String posResponseCode,
            String posResponseMessage,
            String declineCode,
            String declineMessage,
            String failureCode,
            String failureMessage,
            String threeDsSessionId,
            String threeDsFlow,
            Instant createdAt,
            Instant updatedAt,
            Instant processingStartedAt,
            Instant actionRequiredAt,
            Instant authorizedAt,
            Instant authorizationExpiresAt,
            Instant succeededAt,
            Instant declinedAt,
            Instant failedAt,
            Instant canceledAt,
            Instant voidedAt,
            Instant expiredAt
    ) {
        return new PaymentAttempt(
                id,
                paymentIntentId,
                checkoutSessionId,
                status,
                amount,
                currency,
                cardBrand,
                cardLast4,
                posProvider,
                posTransactionId,
                posAuthCode,
                posReference,
                posResponseCode,
                posResponseMessage,
                declineCode,
                declineMessage,
                failureCode,
                failureMessage,
                threeDsSessionId,
                threeDsFlow,
                createdAt,
                updatedAt,
                processingStartedAt,
                actionRequiredAt,
                authorizedAt,
                authorizationExpiresAt,
                succeededAt,
                declinedAt,
                failedAt,
                canceledAt,
                voidedAt,
                expiredAt
        );
    }

    public void markProcessing(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        ensureStatusIn(PaymentAttemptStatus.PROCESSING, PaymentAttemptStatus.INITIATED, PaymentAttemptStatus.REQUIRES_ACTION);

        this.status = PaymentAttemptStatus.PROCESSING;
        this.processingStartedAt = now;
        this.updatedAt = now;
    }

    public void markRequiresAction(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        ensureStatusIn(PaymentAttemptStatus.REQUIRES_ACTION, PaymentAttemptStatus.PROCESSING);

        this.status = PaymentAttemptStatus.REQUIRES_ACTION;
        this.actionRequiredAt = now;
        this.updatedAt = now;
    }

    public void markPending3ds(String threeDsSessionId, String threeDsFlow, Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        Objects.requireNonNull(threeDsSessionId, "threeDsSessionId must not be null");
        ensureStatusIn(PaymentAttemptStatus.REQUIRES_ACTION, PaymentAttemptStatus.PROCESSING);

        this.status = PaymentAttemptStatus.REQUIRES_ACTION;
        this.threeDsSessionId = threeDsSessionId;
        this.threeDsFlow = threeDsFlow;
        this.actionRequiredAt = now;
        this.updatedAt = now;
    }

    public void markAuthorized(
            String posProvider,
            String posTransactionId,
            String posAuthCode,
            String posReference,
            String posResponseCode,
            String posResponseMessage,
            Instant authorizationExpiresAt,
            Instant now
    ) {
        Objects.requireNonNull(now, "now must not be null");
        Objects.requireNonNull(authorizationExpiresAt, "authorizationExpiresAt must not be null");
        ensureStatusIn(PaymentAttemptStatus.AUTHORIZED, PaymentAttemptStatus.PROCESSING, PaymentAttemptStatus.REQUIRES_ACTION);
        assignSuccessfulPosResult(posProvider, posTransactionId, posAuthCode, posReference, posResponseCode, posResponseMessage);

        if (!authorizationExpiresAt.isAfter(now)) {
            throw new IllegalArgumentException("authorizationExpiresAt must be after now");
        }

        this.status = PaymentAttemptStatus.AUTHORIZED;
        this.authorizedAt = now;
        this.authorizationExpiresAt = authorizationExpiresAt;
        this.updatedAt = now;
    }

    public void markSucceeded(
            String posProvider,
            String posTransactionId,
            String posAuthCode,
            String posReference,
            String posResponseCode,
            String posResponseMessage,
            Instant now
    ) {
        Objects.requireNonNull(now, "now must not be null");
        ensureStatusIn(PaymentAttemptStatus.SUCCEEDED, PaymentAttemptStatus.PROCESSING, PaymentAttemptStatus.REQUIRES_ACTION);
        assignSuccessfulPosResult(posProvider, posTransactionId, posAuthCode, posReference, posResponseCode, posResponseMessage);

        this.status = PaymentAttemptStatus.SUCCEEDED;
        this.succeededAt = now;
        this.updatedAt = now;
    }

    public void markCaptured(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        ensureStatusIn(PaymentAttemptStatus.SUCCEEDED, PaymentAttemptStatus.AUTHORIZED);

        this.status = PaymentAttemptStatus.SUCCEEDED;
        this.succeededAt = now;
        this.updatedAt = now;
    }

    public void markDeclined(
            String posProvider,
            String posResponseCode,
            String posResponseMessage,
            String declineCode,
            String declineMessage,
            Instant now
    ) {
        Objects.requireNonNull(now, "now must not be null");
        ensureStatusIn(PaymentAttemptStatus.DECLINED, PaymentAttemptStatus.PROCESSING, PaymentAttemptStatus.REQUIRES_ACTION);

        String validPosProvider = requireText(posProvider, "posProvider must not be blank");
        String validPosResponseCode = requireText(posResponseCode, "posResponseCode must not be blank");
        String validDeclineCode = requireText(declineCode, "declineCode must not be blank");

        this.status = PaymentAttemptStatus.DECLINED;
        this.posProvider = validPosProvider;
        this.posResponseCode = validPosResponseCode;
        this.posResponseMessage = posResponseMessage;
        this.declineCode = validDeclineCode;
        this.declineMessage = declineMessage;
        this.declinedAt = now;
        this.updatedAt = now;
    }

    public void markFailed(
            String failureCode,
            String failureMessage,
            Instant now
    ) {
        Objects.requireNonNull(now, "now must not be null");
        ensureStatusIn(PaymentAttemptStatus.FAILED, PaymentAttemptStatus.PROCESSING, PaymentAttemptStatus.REQUIRES_ACTION);

        String validFailureCode = requireText(failureCode, "failureCode must not be blank");

        this.status = PaymentAttemptStatus.FAILED;
        this.failureCode = validFailureCode;
        this.failureMessage = failureMessage;
        this.failedAt = now;
        this.updatedAt = now;
    }

    public void cancel(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        ensureStatusIn(PaymentAttemptStatus.CANCELED, PaymentAttemptStatus.INITIATED, PaymentAttemptStatus.REQUIRES_ACTION);

        this.status = PaymentAttemptStatus.CANCELED;
        this.canceledAt = now;
        this.updatedAt = now;
    }

    public void voidAuthorization(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        ensureStatusIn(PaymentAttemptStatus.VOIDED, PaymentAttemptStatus.AUTHORIZED);

        this.status = PaymentAttemptStatus.VOIDED;
        this.voidedAt = now;
        this.updatedAt = now;
    }

    public void markRefunded(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        ensureStatusIn(PaymentAttemptStatus.REFUNDED, PaymentAttemptStatus.SUCCEEDED);

        this.status = PaymentAttemptStatus.REFUNDED;
        this.updatedAt = now;
    }

    public void expire(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        ensureStatusIn(PaymentAttemptStatus.EXPIRED, PaymentAttemptStatus.REQUIRES_ACTION, PaymentAttemptStatus.AUTHORIZED);

        this.status = PaymentAttemptStatus.EXPIRED;
        this.expiredAt = now;
        this.updatedAt = now;
    }

    private void assignSuccessfulPosResult(
            String posProvider,
            String posTransactionId,
            String posAuthCode,
            String posReference,
            String posResponseCode,
            String posResponseMessage
    ) {
        this.posProvider = requireText(posProvider, "posProvider must not be blank");
        this.posTransactionId = requireText(posTransactionId, "posTransactionId must not be blank");
        this.posAuthCode = posAuthCode;
        this.posReference = posReference;
        this.posResponseCode = requireText(posResponseCode, "posResponseCode must not be blank");
        this.posResponseMessage = posResponseMessage;
    }

    private void ensureStatusIn(PaymentAttemptStatus targetStatus, PaymentAttemptStatus... allowedStatuses) {
        for (PaymentAttemptStatus allowedStatus : allowedStatuses) {
            if (this.status == allowedStatus) {
                return;
            }
        }

        throw new InvalidPaymentAttemptStateTransitionException(this.status, targetStatus);
    }

    private static BigDecimal validateAmount(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount must not be null");

        if (amount.signum() <= 0) {
            throw new InvalidPaymentAttemptAmountException();
        }

        return amount;
    }

    private static String validateCardLast4(String cardLast4) {
        String validCardLast4 = requireText(cardLast4, "cardLast4 must not be blank");

        if (validCardLast4.length() != 4) {
            throw new IllegalArgumentException("cardLast4 length must be 4");
        }

        return validCardLast4;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }
}
