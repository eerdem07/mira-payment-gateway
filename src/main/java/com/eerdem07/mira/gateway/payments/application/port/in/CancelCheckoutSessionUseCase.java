package com.eerdem07.mira.gateway.payments.application.port.in;

public interface CancelCheckoutSessionUseCase {
    CancelCheckoutSessionResult execute(CancelCheckoutSessionCommand command);
}
