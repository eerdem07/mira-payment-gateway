package com.eerdem07.mira.gateway.payments.pos.dto;

public record MockPosValidationErrorResponse(
        String field,
        String message
) {
}
