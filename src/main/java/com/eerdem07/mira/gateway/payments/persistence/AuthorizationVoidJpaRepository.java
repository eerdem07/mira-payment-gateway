package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.domain.AuthorizationVoidStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthorizationVoidJpaRepository extends JpaRepository<AuthorizationVoidJpaEntity, UUID> {

    boolean existsByPaymentIntentIdAndStatus(UUID paymentIntentId, AuthorizationVoidStatus status);
}
