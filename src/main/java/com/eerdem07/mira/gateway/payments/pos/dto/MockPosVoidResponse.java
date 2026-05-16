package com.eerdem07.mira.gateway.payments.pos.dto;

import java.util.List;

public record MockPosVoidResponse(
        String status,
        String transactionType,
        Boolean approved,
        String responseCode,
        String responseMessage,
        String transactionId,
        String originalTransactionId,
        String posVoidId,
        String originalPosTransactionId,
        String hostReferenceNumber,
        String amount,
        String currency,
        String voidedAt,
        List<MockPosValidationErrorResponse> errors
) {
}
