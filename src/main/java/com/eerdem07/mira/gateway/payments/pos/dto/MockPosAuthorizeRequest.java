package com.eerdem07.mira.gateway.payments.pos.dto;

public record MockPosAuthorizeRequest(
        String merchantId,
        String terminalId,
        String orderId,
        String transactionId,
        String amount,
        String currency,
        int installmentCount,
        boolean capture,
        MockPosCardRequest card
) {
}
