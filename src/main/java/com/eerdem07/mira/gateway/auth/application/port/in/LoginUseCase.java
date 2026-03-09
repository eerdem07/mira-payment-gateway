package com.eerdem07.mira.gateway.auth.application.port.in;

public interface LoginUseCase {
    LoginResult execute(LoginCommand request);
}
