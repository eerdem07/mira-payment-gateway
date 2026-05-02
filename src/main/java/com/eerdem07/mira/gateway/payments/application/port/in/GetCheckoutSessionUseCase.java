package com.eerdem07.mira.gateway.payments.application.port.in;

public interface GetCheckoutSessionUseCase {
    GetCheckoutSessionResult getCheckoutSession(GetCheckoutSessionQuery query);
}
