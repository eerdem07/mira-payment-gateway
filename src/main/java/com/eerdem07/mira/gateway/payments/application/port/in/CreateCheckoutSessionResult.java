package com.eerdem07.mira.gateway.payments.application.port.in;

import com.eerdem07.mira.gateway.payments.domain.CheckoutSessionStatus;

import java.time.Instant;
import java.util.UUID;

public record CreateCheckoutSessionResult(
        UUID id,
        UUID paymentIntentId,
        String token,
        String url,
        CheckoutSessionStatus status,
        Instant expiresAt,
        Instant createdAt
) {}
