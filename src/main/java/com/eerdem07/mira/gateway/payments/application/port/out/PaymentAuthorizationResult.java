package com.eerdem07.mira.gateway.payments.application.port.out;

public record PaymentAuthorizationResult(
        PaymentAuthorizationStatus status,
        String processorTransactionId,
        String authorizationCode,
        String failureCode,
        String failureMessage
) {}
