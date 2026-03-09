package com.eerdem07.mira.gateway.merchants.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class MerchantEmailAlreadyExistsException extends DomainException {
    public MerchantEmailAlreadyExistsException(String email) {
        super("MERCHANT_EMAIL_ALREADY_EXISTS", "Merchant email already exists: " + email);
    }
}
