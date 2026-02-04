package com.eerdem07.mira.gateway.merchants.application.port.out;

import com.eerdem07.mira.gateway.merchants.domain.merchant.Merchant;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepositoryPort {
    Optional<Merchant> findById(UUID merchantId);
    void save(Merchant merchant);
}
