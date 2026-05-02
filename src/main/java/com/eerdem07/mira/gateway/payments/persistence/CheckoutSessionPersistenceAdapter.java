package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.application.port.out.CheckoutSessionRepositoryPort;
import com.eerdem07.mira.gateway.payments.domain.CheckoutSession;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CheckoutSessionPersistenceAdapter implements CheckoutSessionRepositoryPort {

    private final CheckoutSessionJpaRepository repository;

    public CheckoutSessionPersistenceAdapter(CheckoutSessionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CheckoutSession save(CheckoutSession checkoutSession) {
        CheckoutSessionJpaEntity jpaEntity = CheckoutSessionPersistenceMapper.toEntity(checkoutSession);
        CheckoutSessionJpaEntity savedEntity = repository.save(jpaEntity);
        return CheckoutSessionPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CheckoutSession> findByToken(String token) {
        return repository.findByToken(token)
                .map(CheckoutSessionPersistenceMapper::toDomain);
    }
}
