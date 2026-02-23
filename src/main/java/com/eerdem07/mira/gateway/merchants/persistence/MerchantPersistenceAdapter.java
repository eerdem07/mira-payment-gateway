package com.eerdem07.mira.gateway.merchants.persistence;

import com.eerdem07.mira.gateway.merchants.application.port.out.MerchantRepositoryPort;
import com.eerdem07.mira.gateway.merchants.domain.Merchant;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class MerchantPersistenceAdapter implements MerchantRepositoryPort {
    private final SpringDataMerchantRepository repository;

    public MerchantPersistenceAdapter(SpringDataMerchantRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Merchant> findById(UUID merchantId) {
        return repository.findById(merchantId)
                .map(MerchantPersistenceMapper::toDomain);
    }

    @Override
    public void save(Merchant merchant) {
        repository.save(MerchantPersistenceMapper.toEntity(merchant));
    }
}
