package com.eerdem07.mira.gateway.payments.domain;

import com.eerdem07.mira.gateway.payments.domain.exception.PaymentIntentCannotBeCanceledException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentIntentTest {

    private PaymentIntent createPaymentIntentWithStatus(PaymentIntentStatus status) {
        return PaymentIntent.restore(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                Currency.getInstance("USD"),
                "ref-123",
                "desc",
                status,
                0,
                null,
                null,
                Instant.now().plusSeconds(3600),
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(3600),
                null,
                null
        );
    }

    private PaymentIntent createPaymentIntentWithStatusAndCanceledAt(PaymentIntentStatus status, Instant canceledAt) {
        return PaymentIntent.restore(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                Currency.getInstance("USD"),
                "ref-123",
                "desc",
                status,
                0,
                null,
                null,
                Instant.now().plusSeconds(3600),
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(3600),
                null,
                canceledAt
        );
    }

    @Test
    void shouldCancelRequiresPaymentMethodSessionAndSetDates() {
        PaymentIntent intent = createPaymentIntentWithStatus(PaymentIntentStatus.REQUIRES_PAYMENT_METHOD);
        Instant now = Instant.now();

        intent.cancel(now);

        assertThat(intent.getStatus()).isEqualTo(PaymentIntentStatus.CANCELED);
        assertThat(intent.getCanceledAt()).isEqualTo(now);
        assertThat(intent.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldCancelRequiresActionSessionAndSetDates() {
        PaymentIntent intent = createPaymentIntentWithStatus(PaymentIntentStatus.REQUIRES_ACTION);
        Instant now = Instant.now();

        intent.cancel(now);

        assertThat(intent.getStatus()).isEqualTo(PaymentIntentStatus.CANCELED);
        assertThat(intent.getCanceledAt()).isEqualTo(now);
        assertThat(intent.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldReturnIdempotentlyWhenSessionIsAlreadyCanceled() {
        Instant pastCanceledAt = Instant.now().minusSeconds(100);
        PaymentIntent intent = createPaymentIntentWithStatusAndCanceledAt(PaymentIntentStatus.CANCELED, pastCanceledAt);
        Instant now = Instant.now();

        intent.cancel(now);

        assertThat(intent.getStatus()).isEqualTo(PaymentIntentStatus.CANCELED);
        assertThat(intent.getCanceledAt()).isEqualTo(pastCanceledAt);
    }

    @Test
    void shouldThrowExceptionWhenCancelingProcessingSession() {
        PaymentIntent intent = createPaymentIntentWithStatus(PaymentIntentStatus.PROCESSING);
        Instant now = Instant.now();

        assertThatThrownBy(() -> intent.cancel(now))
                .isInstanceOf(PaymentIntentCannotBeCanceledException.class);
    }

    @Test
    void shouldThrowExceptionWhenCancelingSucceededSession() {
        PaymentIntent intent = createPaymentIntentWithStatus(PaymentIntentStatus.SUCCEEDED);
        Instant now = Instant.now();

        assertThatThrownBy(() -> intent.cancel(now))
                .isInstanceOf(PaymentIntentCannotBeCanceledException.class);
    }

    @Test
    void shouldThrowExceptionWhenCancelingFailedSession() {
        PaymentIntent intent = createPaymentIntentWithStatus(PaymentIntentStatus.FAILED);
        Instant now = Instant.now();

        assertThatThrownBy(() -> intent.cancel(now))
                .isInstanceOf(PaymentIntentCannotBeCanceledException.class);
    }

    @Test
    void shouldThrowExceptionWhenCancelingExpiredSession() {
        PaymentIntent intent = createPaymentIntentWithStatus(PaymentIntentStatus.EXPIRED);
        Instant now = Instant.now();

        assertThatThrownBy(() -> intent.cancel(now))
                .isInstanceOf(PaymentIntentCannotBeCanceledException.class);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenNowIsNull() {
        PaymentIntent intent = createPaymentIntentWithStatus(PaymentIntentStatus.REQUIRES_PAYMENT_METHOD);

        assertThatThrownBy(() -> intent.cancel(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("now must not be null");
    }
}
