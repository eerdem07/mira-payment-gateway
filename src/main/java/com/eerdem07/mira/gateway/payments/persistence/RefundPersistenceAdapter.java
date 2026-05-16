package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.application.port.out.RefundRepositoryPort;
import com.eerdem07.mira.gateway.payments.domain.Refund;
import com.eerdem07.mira.gateway.payments.domain.RefundStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RefundPersistenceAdapter implements RefundRepositoryPort {

    private final RefundJpaRepository repository;

    public RefundPersistenceAdapter(RefundJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Refund save(Refund refund) {
        RefundJpaEntity entity = RefundMapper.toEntity(refund);
        RefundJpaEntity savedEntity = repository.save(entity);
        return RefundMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Refund> findById(UUID id) {
        return repository.findById(id)
                .map(RefundMapper::toDomain);
    }

    @Override
    public boolean existsByPaymentIntentIdAndStatus(UUID paymentIntentId, RefundStatus status) {
        return repository.existsByPaymentIntentIdAndStatus(paymentIntentId, status);
    }
}
