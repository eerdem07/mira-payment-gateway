package com.eerdem07.mira.gateway.payments.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentIntentJpaRepository extends JpaRepository<PaymentIntentJpaEntity, UUID> {
    Optional<PaymentIntentJpaEntity> findByIdAndMerchantId(UUID id, UUID merchantId);
    List<PaymentIntentJpaEntity> findAllByMerchantId(UUID merchantId);
}
