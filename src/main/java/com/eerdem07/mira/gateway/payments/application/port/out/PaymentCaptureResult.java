package com.eerdem07.mira.gateway.payments.application.port.out;

public record PaymentCaptureResult(
        PaymentCaptureStatus status,
        String posProvider,
        String posCaptureId,
        String posReference,
        String responseCode,
        String responseMessage,
        String failureCode,
        String failureMessage
) {
}
