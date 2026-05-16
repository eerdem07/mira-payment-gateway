package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.domain.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RefundJpaRepository extends JpaRepository<RefundJpaEntity, UUID> {

    boolean existsByPaymentIntentIdAndStatus(UUID paymentIntentId, RefundStatus status);
}
