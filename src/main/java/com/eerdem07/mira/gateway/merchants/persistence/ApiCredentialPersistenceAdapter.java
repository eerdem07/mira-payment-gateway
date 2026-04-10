package com.eerdem07.mira.gateway.merchants.persistence;

import com.eerdem07.mira.gateway.merchants.application.port.out.ApiCredentialRepositoryPort;
import com.eerdem07.mira.gateway.merchants.domain.ApiCredential;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class ApiCredentialPersistenceAdapter implements ApiCredentialRepositoryPort {
    private final ApiCredentialJpaRepository repository;

    public ApiCredentialPersistenceAdapter(ApiCredentialJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(ApiCredential apiCredential) {
        try {
            repository.saveAndFlush(ApiCredentialPersistenceMapper.toEntity(apiCredential));
        } catch (DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException(ex.getMessage());
        }
    }
}
