package com.eerdem07.mira.gateway.payments.application.port.in;

public interface SubmitCheckoutSessionUseCase {
    SubmitCheckoutSessionResult execute(SubmitCheckoutSessionCommand command);
}
