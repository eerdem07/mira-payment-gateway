package com.eerdem07.mira.gateway.payments.rest.dto;

import java.util.List;

public record ListPaymentIntentsResponse(
        List<GetPaymentIntentResponse> items,
        int count
) {
}
