package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.domain.PaymentAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentAttemptJpaRepository extends JpaRepository<PaymentAttemptJpaEntity, UUID> {

    Optional<PaymentAttemptJpaEntity> findFirstByPaymentIntentIdAndStatusOrderByCreatedAtDesc(
            UUID paymentIntentId,
            PaymentAttemptStatus status
    );
}
