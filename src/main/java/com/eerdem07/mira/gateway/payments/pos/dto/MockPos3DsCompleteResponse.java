package com.eerdem07.mira.gateway.payments.pos.dto;

public record MockPos3DsCompleteResponse(
        String status,
        String transactionType,
        Boolean approved,
        String responseCode,
        String responseMessage,
        String transactionId,
        String originalTransactionId,
        String posTransactionId,
        String authCode,
        String hostReferenceNumber,
        String amount,
        String currency,
        Integer installmentCount,
        String installmentAmount,
        String authorizedAt,
        String threeDsSessionId,
        String threeDsStatus,
        String eci,
        String messageVersion
) {
}
