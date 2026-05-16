package com.eerdem07.mira.gateway.payments.application.port.out;

public record PaymentRefundResult(
        PaymentRefundStatus status,
        String posProvider,
        String posRefundId,
        String posReference,
        String responseCode,
        String responseMessage,
        String failureCode,
        String failureMessage
) {
}
