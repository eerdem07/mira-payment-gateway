package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAttemptPort;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttempt;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttemptStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentAttemptPersistenceAdapter implements PaymentAttemptPort {

    private final PaymentAttemptJpaRepository repository;

    public PaymentAttemptPersistenceAdapter(PaymentAttemptJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PaymentAttempt save(PaymentAttempt paymentAttempt) {
        PaymentAttemptJpaEntity entity = PaymentAttemptMapper.toEntity(paymentAttempt);
        PaymentAttemptJpaEntity savedEntity = repository.save(entity);
        return PaymentAttemptMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PaymentAttempt> findById(UUID id) {
        return repository.findById(id)
                .map(PaymentAttemptMapper::toDomain);
    }

    @Override
    public Optional<PaymentAttempt> findLatestByPaymentIntentIdAndStatus(UUID paymentIntentId, PaymentAttemptStatus status) {
        return repository.findFirstByPaymentIntentIdAndStatusOrderByCreatedAtDesc(paymentIntentId, status)
                .map(PaymentAttemptMapper::toDomain);
    }
}
