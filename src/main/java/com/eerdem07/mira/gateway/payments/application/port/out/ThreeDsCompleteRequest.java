package com.eerdem07.mira.gateway.payments.application.port.out;

import java.util.UUID;

public record ThreeDsCompleteRequest(
        UUID paymentIntentId,
        UUID paymentAttemptId,
        String orderId,
        String threeDsSessionId
) {
}
