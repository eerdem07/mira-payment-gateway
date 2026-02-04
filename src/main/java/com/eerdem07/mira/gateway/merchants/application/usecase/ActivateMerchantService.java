package com.eerdem07.mira.gateway.merchants.application.usecase;

import com.eerdem07.mira.gateway.merchants.application.exception.MerchantNotFoundException;
import com.eerdem07.mira.gateway.merchants.application.port.in.ActivateMerchantUseCase;
import com.eerdem07.mira.gateway.merchants.application.port.out.MerchantRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class ActivateMerchantService implements ActivateMerchantUseCase {

    private final MerchantRepositoryPort merchantRepository;
    private final Clock clock;

    public ActivateMerchantService(MerchantRepositoryPort merchantRepository, Clock clock) {
        this.merchantRepository = merchantRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void activate(UUID merchantId) {
        var merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        merchant.activate(clock);

        merchantRepository.save(merchant);
    }
}
