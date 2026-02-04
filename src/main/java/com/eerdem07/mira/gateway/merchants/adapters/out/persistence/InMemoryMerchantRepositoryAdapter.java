package com.eerdem07.mira.gateway.merchants.adapters.out.persistence;

import com.eerdem07.mira.gateway.merchants.application.port.out.MerchantRepositoryPort;
import com.eerdem07.mira.gateway.merchants.domain.merchant.Merchant;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryMerchantRepositoryAdapter implements MerchantRepositoryPort {

    private final Map<UUID, Merchant> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Merchant> findById(UUID merchantId) {
        return Optional.ofNullable(store.get(merchantId));
    }

    @Override
    public void save(Merchant merchant) {
        store.put(merchant.getMerchantId(), merchant);
    }
}
