package com.eerdem07.mira.gateway.payments.rest.dto;

import com.eerdem07.mira.gateway.payments.domain.PaymentIntentStatus;

import java.util.UUID;

public record CancelPaymentIntentResponse(
        UUID id,
        PaymentIntentStatus status
) {
}
