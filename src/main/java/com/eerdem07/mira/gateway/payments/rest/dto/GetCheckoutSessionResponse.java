package com.eerdem07.mira.gateway.payments.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record GetCheckoutSessionResponse(
        String id,
        String status,
        Instant expiresAt,
        String returnUrl,
        String cancelUrl,
        PaymentDetails payment
) {
    public record PaymentDetails(
            BigDecimal amount,
            String currency,
            String description
    ) {}
}
