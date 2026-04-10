package com.eerdem07.mira.gateway.merchants.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApiCredentialJpaRepository extends JpaRepository<ApiCredentialJpaEntity, UUID> {
}
