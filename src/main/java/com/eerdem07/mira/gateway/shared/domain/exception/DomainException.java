package com.eerdem07.mira.gateway.shared.domain.exception;

public class DomainException extends RuntimeException {
    private final String code;

    protected DomainException(String code, String message){
        super(message);
        this.code = code;
    }

    public String code(){
        return code;
    }
}
