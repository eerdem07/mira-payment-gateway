package com.eerdem07.mira.gateway.payments.application.port.in;

public interface ListPaymentIntentsUseCase {
    ListPaymentIntentsResult execute(ListPaymentIntentsQuery query);
}
