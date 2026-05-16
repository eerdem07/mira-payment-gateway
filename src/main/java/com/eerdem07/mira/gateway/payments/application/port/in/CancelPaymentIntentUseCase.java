package com.eerdem07.mira.gateway.payments.application.port.in;

public interface CancelPaymentIntentUseCase {
    CancelPaymentIntentResult execute(CancelPaymentIntentCommand command);
}
