package com.eerdem07.mira.gateway.payments.application.port.in;

import java.math.BigDecimal;
import java.time.Instant;

public record CreatePaymentIntentResult(String id,
                                        String status,
                                        BigDecimal amount,
                                        String currency,
                                        String merchantReference,
                                        String description,
                                        Instant expiresAt,
                                        Instant createdAt
) {
}
