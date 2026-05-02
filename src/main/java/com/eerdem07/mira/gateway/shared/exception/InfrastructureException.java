package com.eerdem07.mira.gateway.shared.exception;

public abstract class InfrastructureException extends RuntimeException {
    private final String code;

    public InfrastructureException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
