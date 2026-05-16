package com.eerdem07.mira.gateway.payments.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentAttemptTest {

    @Test
    void shouldCreatePaymentAttemptWithCreatedAndUpdatedAt() {
        Instant now = Instant.parse("2026-05-13T13:46:30.515951Z");

        PaymentAttempt paymentAttempt = PaymentAttempt.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                Currency.getInstance("TRY"),
                "VISA",
                "4242",
                now
        );

        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.INITIATED);
        assertThat(paymentAttempt.getCreatedAt()).isEqualTo(now);
        assertThat(paymentAttempt.getUpdatedAt()).isEqualTo(now);
    }
}
