package com.eerdem07.mira.gateway.payments.application.port.out;

public interface PaymentProcessorPort {
    PaymentAuthorizationResult authorize(PaymentAuthorizationRequest request);
}
