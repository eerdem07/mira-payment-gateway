package com.eerdem07.mira.gateway.payments.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentIntentCommand(UUID merchantId, BigDecimal amount, String currency, String merchantReference, String description) {
}
