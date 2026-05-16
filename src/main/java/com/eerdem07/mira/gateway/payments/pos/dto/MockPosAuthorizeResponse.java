package com.eerdem07.mira.gateway.payments.pos.dto;

import java.util.List;

public record MockPosAuthorizeResponse(
        String status,
        String transactionType,
        Boolean approved,
        String responseCode,
        String responseMessage,
        String transactionId,
        String posTransactionId,
        String authCode,
        String hostReferenceNumber,
        String amount,
        String currency,
        Integer installmentCount,
        String authorizedAt,
        String threeDsSessionId,
        String acsUrl,
        String threeDsFlow,
        String expiresAt,
        String messageVersion,
        List<MockPosValidationErrorResponse> errors
) {
}
