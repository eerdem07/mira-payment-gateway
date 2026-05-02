package com.eerdem07.mira.gateway.payments.application.port.in;

public interface GetPaymentIntentUseCase {
    GetPaymentIntentResult execute(GetPaymentIntentQuery query);
}
