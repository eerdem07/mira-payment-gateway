package com.eerdem07.mira.gateway.payments.application.port.in;

import com.eerdem07.mira.gateway.payments.domain.PaymentIntentStatus;

import java.util.UUID;

public record CancelPaymentIntentResult(
        UUID id,
        PaymentIntentStatus status
) {
}
