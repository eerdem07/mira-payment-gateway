package com.eerdem07.mira.gateway.merchants.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class InvalidEmailException extends DomainException {
    public InvalidEmailException(String message) {
        super("INVALID_EMAIL", message);
    }
}

//public class InvalidEmailException extends IllegalArgumentException {
//    public InvalidEmailException(String message) {
//        super(message);
//    }
//}
