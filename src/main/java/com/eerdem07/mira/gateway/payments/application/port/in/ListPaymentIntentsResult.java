package com.eerdem07.mira.gateway.payments.application.port.in;

import java.util.List;

public record ListPaymentIntentsResult(
        List<GetPaymentIntentResult> items
) {
}
