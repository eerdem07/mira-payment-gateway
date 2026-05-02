package com.eerdem07.mira.gateway.payments.application.port.in;

public record SubmitCheckoutSessionCommand(
        String token,
        String cardNumber,
        String expiryMonth,
        String expiryYear,
        String cvc,
        String cardHolderName
) {}
