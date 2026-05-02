package com.eerdem07.mira.gateway.merchants.persistence;

import com.eerdem07.mira.gateway.merchants.application.port.out.ApiCredentialRepositoryPort;
import com.eerdem07.mira.gateway.merchants.domain.ApiCredential;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ApiCredentialPersistenceAdapter implements ApiCredentialRepositoryPort {
    private final ApiCredentialJpaRepository repository;

    public ApiCredentialPersistenceAdapter(ApiCredentialJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ApiCredential save(ApiCredential apiCredential) {
        ApiCredentialJpaEntity entity = ApiCredentialPersistenceMapper.toEntity(apiCredential);
        ApiCredentialJpaEntity saved =  repository.save(entity);
        return ApiCredentialPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<ApiCredential> findByKeyId(String keyId) {
        return repository.findByKeyId(keyId)
                .map(ApiCredentialPersistenceMapper::toDomain);
    }
}
