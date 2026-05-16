package com.eerdem07.mira.gateway.payments.pos.dto;

public record MockPosCardRequest(
        String holderName,
        String pan,
        String expiryMonth,
        String expiryYear,
        String cvv
) {
}
