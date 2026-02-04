package com.eerdem07.mira.gateway.merchants.application.port.in;

public interface RegisterMerchantUseCase {
    RegisterMerchantResult register(RegisterMerchantCommand command);
}
