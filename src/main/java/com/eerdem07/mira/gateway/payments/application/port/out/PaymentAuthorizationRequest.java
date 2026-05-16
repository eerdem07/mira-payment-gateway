package com.eerdem07.mira.gateway.payments.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentAuthorizationRequest(
        UUID paymentIntentId,
        UUID paymentAttemptId,
        UUID merchantId,
        BigDecimal amount,
        String currency,
        String orderId,
        int installmentCount,
        boolean capture,
        String cardNumber,
        String expiryMonth,
        String expiryYear,
        String cvc,
        String cardHolderName
) {}
