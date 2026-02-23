package com.eerdem07.mira.gateway.merchants.application.port.in;

import java.util.UUID;

public interface ActivateMerchantUseCase {
    void execute(UUID merchantId);
}
