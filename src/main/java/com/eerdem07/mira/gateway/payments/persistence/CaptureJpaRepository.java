package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.domain.CaptureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CaptureJpaRepository extends JpaRepository<CaptureJpaEntity, UUID> {

    boolean existsByPaymentIntentIdAndStatus(UUID paymentIntentId, CaptureStatus status);
}
