package com.eerdem07.mira.gateway.payments.rest.dto;

import com.eerdem07.mira.gateway.payments.domain.AuthorizationVoidStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateAuthorizationVoidResponse(
        UUID id,
        UUID paymentIntentId,
        UUID paymentAttemptId,
        AuthorizationVoidStatus status,
        BigDecimal amount,
        String currency,
        String posProvider,
        String posVoidId,
        String posReference,
        String posResponseCode,
        String posResponseMessage,
        String failureCode,
        String failureMessage,
        Instant createdAt,
        Instant updatedAt,
        Instant succeededAt,
        Instant failedAt
) {
}
