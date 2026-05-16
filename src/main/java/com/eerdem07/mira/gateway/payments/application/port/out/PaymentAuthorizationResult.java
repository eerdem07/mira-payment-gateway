package com.eerdem07.mira.gateway.payments.application.port.out;

public record PaymentAuthorizationResult(
        PaymentAuthorizationStatus status,
        String posProvider,
        String posTransactionId,
        String authorizationCode,
        String posReference,
        String responseCode,
        String responseMessage,
        String failureCode,
        String failureMessage,
        String threeDsSessionId,
        String acsUrl,
        String threeDsFlow
) {}
