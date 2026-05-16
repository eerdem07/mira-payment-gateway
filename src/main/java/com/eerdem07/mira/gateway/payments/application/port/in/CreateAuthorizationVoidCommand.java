package com.eerdem07.mira.gateway.payments.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateAuthorizationVoidCommand(
        UUID merchantId,
        UUID paymentIntentId,
        BigDecimal amount,
        String currency
) {
}
