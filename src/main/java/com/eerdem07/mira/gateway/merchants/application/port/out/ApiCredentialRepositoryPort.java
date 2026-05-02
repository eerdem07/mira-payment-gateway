package com.eerdem07.mira.gateway.merchants.application.port.out;

import com.eerdem07.mira.gateway.merchants.domain.ApiCredential;

import java.util.Optional;

public interface ApiCredentialRepositoryPort {
    ApiCredential save(ApiCredential apiCredential);
    Optional<ApiCredential> findByKeyId(String keyId);
}
