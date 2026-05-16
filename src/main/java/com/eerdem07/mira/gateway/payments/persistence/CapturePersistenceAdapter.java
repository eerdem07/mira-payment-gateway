package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.application.port.out.CaptureRepositoryPort;
import com.eerdem07.mira.gateway.payments.domain.Capture;
import com.eerdem07.mira.gateway.payments.domain.CaptureStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CapturePersistenceAdapter implements CaptureRepositoryPort {

    private final CaptureJpaRepository repository;

    public CapturePersistenceAdapter(CaptureJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Capture save(Capture capture) {
        CaptureJpaEntity entity = CaptureMapper.toEntity(capture);
        CaptureJpaEntity savedEntity = repository.save(entity);
        return CaptureMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Capture> findById(UUID id) {
        return repository.findById(id)
                .map(CaptureMapper::toDomain);
    }

    @Override
    public boolean existsByPaymentIntentIdAndStatus(UUID paymentIntentId, CaptureStatus status) {
        return repository.existsByPaymentIntentIdAndStatus(paymentIntentId, status);
    }
}
