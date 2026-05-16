package com.eerdem07.mira.gateway.payments.rest.dto;

import com.eerdem07.mira.gateway.payments.domain.CaptureStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateCaptureResponse(
        UUID id,
        UUID paymentIntentId,
        UUID paymentAttemptId,
        CaptureStatus status,
        BigDecimal amount,
        String currency,
        String posProvider,
        String posCaptureId,
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
