package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentIntentPersistenceAdapter implements PaymentIntentRepositoryPort {

    private final PaymentIntentJpaRepository repository;

    public PaymentIntentPersistenceAdapter(PaymentIntentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PaymentIntent save(PaymentIntent intent) {
        PaymentIntentJpaEntity entity = PaymentIntentPersistenceMapper.toEntity(intent);
        PaymentIntentJpaEntity savedEntity = repository.save(entity);
        return PaymentIntentPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PaymentIntent> findById(UUID id) {
        return repository.findById(id)
                .map(PaymentIntentPersistenceMapper::toDomain);
    }
}
