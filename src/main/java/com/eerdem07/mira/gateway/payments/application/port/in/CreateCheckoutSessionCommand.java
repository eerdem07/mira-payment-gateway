package com.eerdem07.mira.gateway.payments.application.port.in;

import java.util.UUID;

public record CreateCheckoutSessionCommand(
        UUID paymentIntentId,
        String returnUrl,
        String cancelUrl
) {}
