package com.eerdem07.mira.gateway.payments.pos.dto;

public record MockPosVoidRequest(
        String merchantId,
        String terminalId,
        String orderId,
        String transactionId,
        String originalTransactionId,
        String originalPosTransactionId,
        String authCode,
        String hostReferenceNumber,
        String amount,
        String currency
) {
}
