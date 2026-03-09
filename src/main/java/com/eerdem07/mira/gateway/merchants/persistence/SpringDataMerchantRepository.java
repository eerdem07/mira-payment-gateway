package com.eerdem07.mira.gateway.merchants.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface SpringDataMerchantRepository extends JpaRepository<MerchantJpaEntity, UUID> {
    Optional<MerchantJpaEntity> findByEmail(String email);
}
