package com.eerdem07.mira.gateway.payments.application.port.in;

public interface CreateRefundUseCase {

    CreateRefundResult execute(CreateRefundCommand command);
}
