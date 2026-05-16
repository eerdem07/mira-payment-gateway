package com.eerdem07.mira.gateway.payments.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCaptureRequest(
        UUID paymentIntentId,
        UUID captureId,
        UUID originalPaymentAttemptId,
        UUID merchantId,
        BigDecimal amount,
        String currency,
        String orderId,
        String originalPosTransactionId,
        String authorizationCode,
        String posReference
) {
}
