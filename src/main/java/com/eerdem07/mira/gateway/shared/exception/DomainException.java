package com.eerdem07.mira.gateway.shared.exception;

import lombok.Getter;

@Getter
public abstract class DomainException extends RuntimeException {

    private final String code;
    private final ApplicationException.ErrorType type;

    protected DomainException(String code, String message) {
        this(ApplicationException.ErrorType.UNPROCESSABLE_ENTITY, code, message, null);
    }

    protected DomainException(String code, String message, Throwable cause) {
        this(ApplicationException.ErrorType.UNPROCESSABLE_ENTITY, code, message, cause);
    }

    protected DomainException(ApplicationException.ErrorType type, String code, String message) {
        this(type, code, message, null);
    }

    protected DomainException(ApplicationException.ErrorType type, String code, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.code = code;
    }

}