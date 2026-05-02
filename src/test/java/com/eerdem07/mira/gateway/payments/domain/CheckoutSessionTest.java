package com.eerdem07.mira.gateway.payments.domain;

import com.eerdem07.mira.gateway.payments.domain.exception.CheckoutSessionCannotBeCanceledException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckoutSessionTest {

    private CheckoutSession createSessionWithStatus(CheckoutSessionStatus status) {
        return CheckoutSession.restore(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "token",
                status,
                "https://return.url",
                "https://cancel.url",
                Instant.now().plusSeconds(3600),
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(3600),
                null,
                null
        );
    }

    @Test
    void shouldCancelOpenSessionAndSetDates() {
        CheckoutSession session = createSessionWithStatus(CheckoutSessionStatus.OPEN);
        Instant now = Instant.now();

        session.cancel(now);

        assertThat(session.getStatus()).isEqualTo(CheckoutSessionStatus.CANCELED);
        assertThat(session.getCanceledAt()).isEqualTo(now);
        assertThat(session.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldReturnIdempotentlyWhenSessionIsAlreadyCanceled() {
        CheckoutSession session = createSessionWithStatus(CheckoutSessionStatus.CANCELED);
        Instant now = Instant.now();

        session.cancel(now);

        // Status should still be CANCELED and it shouldn't throw any exception
        assertThat(session.getStatus()).isEqualTo(CheckoutSessionStatus.CANCELED);
    }

    @Test
    void shouldThrowExceptionWhenCancelingSubmittedSession() {
        CheckoutSession session = createSessionWithStatus(CheckoutSessionStatus.SUBMITTED);
        Instant now = Instant.now();

        assertThatThrownBy(() -> session.cancel(now))
                .isInstanceOf(CheckoutSessionCannotBeCanceledException.class);
    }

    @Test
    void shouldThrowExceptionWhenCancelingExpiredSession() {
        CheckoutSession session = createSessionWithStatus(CheckoutSessionStatus.EXPIRED);
        Instant now = Instant.now();

        assertThatThrownBy(() -> session.cancel(now))
                .isInstanceOf(CheckoutSessionCannotBeCanceledException.class);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenNowIsNull() {
        CheckoutSession session = createSessionWithStatus(CheckoutSessionStatus.OPEN);

        assertThatThrownBy(() -> session.cancel(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("now must not be null");
    }
}
