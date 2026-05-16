package com.eerdem07.mira.gateway.payments.pos.dto;

import java.util.List;

public record MockPosCaptureResponse(
        String status,
        String transactionType,
        Boolean approved,
        String responseCode,
        String responseMessage,
        String transactionId,
        String originalTransactionId,
        String posCaptureId,
        String originalPosTransactionId,
        String hostReferenceNumber,
        String amount,
        String currency,
        String capturedAt,
        List<MockPosValidationErrorResponse> errors
) {
}
