package com.eerdem07.mira.gateway.payments.pos.dto;

import java.util.List;

public record MockPosRefundResponse(
        String status,
        String transactionType,
        Boolean approved,
        String responseCode,
        String responseMessage,
        String transactionId,
        String originalTransactionId,
        String posRefundId,
        String originalPosTransactionId,
        String hostReferenceNumber,
        String amount,
        String currency,
        String refundedAt,
        List<MockPosValidationErrorResponse> errors
) {
}
