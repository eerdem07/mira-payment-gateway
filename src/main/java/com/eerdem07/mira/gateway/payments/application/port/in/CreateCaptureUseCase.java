package com.eerdem07.mira.gateway.payments.application.port.in;

public interface CreateCaptureUseCase {

    CreateCaptureResult execute(CreateCaptureCommand command);
}
