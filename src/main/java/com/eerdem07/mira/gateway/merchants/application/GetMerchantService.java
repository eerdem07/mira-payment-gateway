package com.eerdem07.mira.gateway.merchants.application;

import com.eerdem07.mira.gateway.merchants.application.exception.MerchantNotFoundException;
import com.eerdem07.mira.gateway.merchants.application.port.in.GetMerchantQuery;
import com.eerdem07.mira.gateway.merchants.application.port.in.GetMerchantResult;
import com.eerdem07.mira.gateway.merchants.application.port.in.GetMerchantUseCase;
import com.eerdem07.mira.gateway.merchants.application.port.out.MerchantRepositoryPort;
import com.eerdem07.mira.gateway.merchants.domain.Merchant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetMerchantService implements GetMerchantUseCase {
    private final MerchantRepositoryPort merchantRepositoryPort;

    public GetMerchantService(MerchantRepositoryPort merchantRepositoryPort) {
        this.merchantRepositoryPort = merchantRepositoryPort;
    }

    @Transactional(readOnly = true)
    public GetMerchantResult execute(GetMerchantQuery query) {
        Merchant merchant = this.merchantRepositoryPort.findById(query.merchantId())
                .orElseThrow(() -> new MerchantNotFoundException(query.merchantId()));

        return new GetMerchantResult(
                merchant.getMerchantId(),
                merchant.getLegalName(),
                merchant.getStatus()
        );
    }
}

// MERCHANT NOT FOUND
