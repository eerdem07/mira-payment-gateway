package com.eerdem07.mira.gateway.auth.application.port.in;

public record LoginCommand(String email, String password) {
}
