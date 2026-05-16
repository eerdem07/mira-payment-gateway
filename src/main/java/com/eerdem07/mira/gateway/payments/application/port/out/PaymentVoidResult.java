package com.eerdem07.mira.gateway.payments.application.port.out;

public record PaymentVoidResult(
        PaymentVoidStatus status,
        String posProvider,
        String posVoidId,
        String posReference,
        String responseCode,
        String responseMessage,
        String failureCode,
        String failureMessage
) {
}
