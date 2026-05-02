package com.eerdem07.mira.gateway.payments.application.port.in;

public interface CreatePaymentIntentUseCase {
    CreatePaymentIntentResult execute(CreatePaymentIntentCommand command);
}
