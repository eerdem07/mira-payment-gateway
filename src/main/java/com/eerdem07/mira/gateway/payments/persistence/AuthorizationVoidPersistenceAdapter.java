package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.application.port.out.AuthorizationVoidRepositoryPort;
import com.eerdem07.mira.gateway.payments.domain.AuthorizationVoid;
import com.eerdem07.mira.gateway.payments.domain.AuthorizationVoidStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuthorizationVoidPersistenceAdapter implements AuthorizationVoidRepositoryPort {

    private final AuthorizationVoidJpaRepository repository;

    public AuthorizationVoidPersistenceAdapter(AuthorizationVoidJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuthorizationVoid save(AuthorizationVoid authorizationVoid) {
        AuthorizationVoidJpaEntity entity = AuthorizationVoidMapper.toEntity(authorizationVoid);
        AuthorizationVoidJpaEntity savedEntity = repository.save(entity);
        return AuthorizationVoidMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<AuthorizationVoid> findById(UUID id) {
        return repository.findById(id)
                .map(AuthorizationVoidMapper::toDomain);
    }

    @Override
    public boolean existsByPaymentIntentIdAndStatus(UUID paymentIntentId, AuthorizationVoidStatus status) {
        return repository.existsByPaymentIntentIdAndStatus(paymentIntentId, status);
    }
}
