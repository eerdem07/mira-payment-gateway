package com.eerdem07.mira.gateway.merchants.application.port.in;

public interface GetMerchantUseCase {
    GetMerchantResult execute(GetMerchantQuery merchantId);
}
