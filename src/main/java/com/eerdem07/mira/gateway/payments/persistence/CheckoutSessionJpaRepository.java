package com.eerdem07.mira.gateway.payments.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CheckoutSessionJpaRepository extends JpaRepository<CheckoutSessionJpaEntity, UUID> {
    Optional<CheckoutSessionJpaEntity> findByToken(String token);
}
