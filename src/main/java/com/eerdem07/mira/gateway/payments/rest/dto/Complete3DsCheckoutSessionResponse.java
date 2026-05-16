package com.eerdem07.mira.gateway.payments.rest.dto;

import com.eerdem07.mira.gateway.payments.domain.CheckoutSessionStatus;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntentStatus;

import java.util.UUID;

public record Complete3DsCheckoutSessionResponse(
        UUID checkoutSessionId,
        UUID paymentIntentId,
        CheckoutSessionStatus checkoutSessionStatus,
        PaymentIntentStatus paymentIntentStatus,
        String returnUrl,
        String failureCode,
        String failureMessage
) {
}
