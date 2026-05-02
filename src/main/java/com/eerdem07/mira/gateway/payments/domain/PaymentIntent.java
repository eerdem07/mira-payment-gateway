    package com.eerdem07.mira.gateway.payments.domain;

    import com.eerdem07.mira.gateway.payments.domain.exception.PaymentIntentCannotBeCanceledException;
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
        private final String merchantReference;
        // merchantReference, merchant tarafındaki müşterinin sipariş id'sini tutyor.
        private final String description;
        private PaymentIntentStatus status;
        private int attemptCount;
        private String failureCode;
        private String failureMessage;
        private Instant expiresAt;
        private final Instant createdAt;
        private Instant updatedAt;
        private Instant succeededAt;
        private Instant canceledAt;

        private PaymentIntent(UUID id, UUID merchantId, BigDecimal amount, Currency currency,
                              String merchantReference, String description, PaymentIntentStatus status,
                              int attemptCount, String failureCode, String failureMessage,
                              Instant expiresAt, Instant createdAt, Instant updatedAt,
                              Instant succeededAt, Instant canceledAt) {
            this.id = Objects.requireNonNull(id, "id");
            this.merchantId = Objects.requireNonNull(merchantId, "merchantId");
            this.amount = Objects.requireNonNull(amount, "amount");
            this.currency = Objects.requireNonNull(currency, "currency");
            this.merchantReference = merchantReference;
            this.description = description;
            this.status = Objects.requireNonNull(status, "status");
            this.attemptCount = attemptCount;
            this.failureCode = failureCode;
            this.failureMessage = failureMessage;
            this.expiresAt = expiresAt;
            this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
            this.updatedAt = updatedAt;
            this.succeededAt = succeededAt;
            this.canceledAt = canceledAt;
        }

        public static PaymentIntent create(UUID id, UUID merchantId, BigDecimal amount, Currency currency,
                                           String merchantReference, String description, Instant now, Instant expiresAt) {
            return new PaymentIntent(
                    id,
                    merchantId,
                    amount,
                    currency,
                    merchantReference,
                    description,
                    PaymentIntentStatus.REQUIRES_PAYMENT_METHOD,
                    0,
                    null,
                    null,
                    expiresAt,
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
            return new PaymentIntent(
                    id, merchantId, amount, currency, merchantReference, description, status,
                    attemptCount, failureCode, failureMessage, expiresAt, createdAt, updatedAt,
                    succeededAt, canceledAt
            );
        }

        // domain behaviors:
        // attachPaymentMethod(...)
        // markRequiresConfirmation()
        // markProcessing()
        // markSucceeded()
        // markFailed(...)
        // cancel()
        // expire()
        // validateConfirmable()

        public void attachPaymentMethod(Instant now) {
            if (this.status != PaymentIntentStatus.REQUIRES_PAYMENT_METHOD) {
                throw new IllegalStateException("PaymentIntent is not in REQUIRES_PAYMENT_METHOD status");
            }
            this.status = PaymentIntentStatus.REQUIRES_CONFIRMATION;
            this.updatedAt = Objects.requireNonNull(now, "now");
        }

        public void markRequiresConfirmation(Instant now) {
            this.status = PaymentIntentStatus.REQUIRES_CONFIRMATION;
            this.updatedAt = Objects.requireNonNull(now, "now");
        }

        public void markProcessing(Instant now) {
            this.status = PaymentIntentStatus.PROCESSING;
            this.updatedAt = Objects.requireNonNull(now, "now");
        }

        public void markSucceeded(Instant now) {
            this.status = PaymentIntentStatus.SUCCEEDED;
            this.succeededAt = Objects.requireNonNull(now, "now");
            this.updatedAt = now;
        }

        public void markFailed(String failureCode, String failureMessage, Instant now) {
            this.status = PaymentIntentStatus.FAILED;
            this.failureCode = failureCode;
            this.failureMessage = failureMessage;
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
                || this.status == PaymentIntentStatus.REQUIRES_CONFIRMATION;
        }

        public void expire(Instant now) {
            if (this.status != PaymentIntentStatus.REQUIRES_PAYMENT_METHOD && this.status != PaymentIntentStatus.REQUIRES_CONFIRMATION) {
                 throw new IllegalStateException("Only pending intents can be expired");
            }
            this.status = PaymentIntentStatus.EXPIRED;
            this.updatedAt = Objects.requireNonNull(now, "now");
        }

        public void validateConfirmable() {
            if (this.status != PaymentIntentStatus.REQUIRES_CONFIRMATION) {
                throw new IllegalStateException("PaymentIntent is not ready to be confirmed");
            }
        }
    }


    // bir adet Money ValueObject kurulacak. şimdilik amount ve currency kullandık.
