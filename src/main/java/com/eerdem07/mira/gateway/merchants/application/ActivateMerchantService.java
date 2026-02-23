package com.eerdem07.mira.gateway.merchants.application;

import com.eerdem07.mira.gateway.merchants.application.exception.MerchantNotFoundException;
import com.eerdem07.mira.gateway.merchants.application.port.in.ActivateMerchantUseCase;
import com.eerdem07.mira.gateway.merchants.application.port.out.MerchantRepositoryPort;
import com.eerdem07.mira.gateway.merchants.domain.Merchant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
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
    public void execute(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        Instant now = Instant.now(clock);
        merchant.activate(now);

        merchantRepository.save(merchant);
    }
}
