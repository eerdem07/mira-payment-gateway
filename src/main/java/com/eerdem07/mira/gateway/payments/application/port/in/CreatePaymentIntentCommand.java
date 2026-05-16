package com.eerdem07.mira.gateway.payments.application.port.in;

import com.eerdem07.mira.gateway.payments.domain.CaptureMethod;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentIntentCommand(
        UUID merchantId,
        BigDecimal amount,
        String currency,
        CaptureMethod captureMethod,
        String merchantReference,
        String description
) {
}
