package com.eerdem07.mira.gateway.payments.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CreatePaymentIntentResponse(
        String id,
        String status,
        BigDecimal amount,
        String currency,
        String merchantReference,
        String description,
        Instant expiresAt,
        Instant createdAt
) {
}