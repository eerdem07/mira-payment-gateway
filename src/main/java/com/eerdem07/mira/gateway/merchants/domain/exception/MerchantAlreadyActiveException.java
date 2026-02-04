package com.eerdem07.mira.gateway.merchants.domain.exception;

import com.eerdem07.mira.gateway.shared.domain.exception.DomainException;

public class MerchantAlreadyActiveException  extends DomainException {
    public MerchantAlreadyActiveException(){
        super("MERCHANT_ALREADY_ACTIVE","Merchant is alreadt active.");
    }
}
