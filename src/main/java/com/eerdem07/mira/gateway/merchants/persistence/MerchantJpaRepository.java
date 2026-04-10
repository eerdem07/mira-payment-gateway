package com.eerdem07.mira.gateway.merchants.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MerchantJpaRepository extends JpaRepository<MerchantJpaEntity, UUID> {
    Optional<MerchantJpaEntity> findByEmail(String email);
}
