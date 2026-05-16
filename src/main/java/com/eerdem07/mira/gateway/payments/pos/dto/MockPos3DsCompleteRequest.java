package com.eerdem07.mira.gateway.payments.pos.dto;

public record MockPos3DsCompleteRequest(
        String merchantId,
        String terminalId,
        String orderId,
        String transactionId,
        String threeDsSessionId
) {
}
