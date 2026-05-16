package com.eerdem07.mira.gateway.payments.application.port.in;

public interface CreateAuthorizationVoidUseCase {

    CreateAuthorizationVoidResult execute(CreateAuthorizationVoidCommand command);
}
