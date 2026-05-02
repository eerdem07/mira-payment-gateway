package com.eerdem07.mira.gateway.payments.application.port.in;

public interface CreateCheckoutSessionUseCase {
    CreateCheckoutSessionResult execute(CreateCheckoutSessionCommand command);
}
