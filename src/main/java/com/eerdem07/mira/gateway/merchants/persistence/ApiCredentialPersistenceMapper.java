package com.eerdem07.mira.gateway.merchants.persistence;

import com.eerdem07.mira.gateway.merchants.domain.ApiCredential;

public final class ApiCredentialPersistenceMapper {
    private ApiCredentialPersistenceMapper() {
    }

    public static ApiCredentialJpaEntity toEntity(ApiCredential domain) {
        if (domain == null) {
            return null;
        }

        ApiCredentialJpaEntity entity = new ApiCredentialJpaEntity(
                domain.getCredentialId(),
                domain.getMerchantId(),
                domain.getKeyId(),
                domain.getSecretHash(),
                domain.getSecretSuffix(),
                domain.getApiCredentialEnvironment(),
                domain.getApiCredentialStatus(),
                domain.getCreatedAt(),
                domain.getLastUsedAt(),
                domain.getRevokedAt()
        );
        entity.setVersion(domain.getVersion());
        return entity;
    }

    public static ApiCredential toDomain(ApiCredentialJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        ApiCredential domain = ApiCredential.restore(
                entity.getCredentialId(),
                entity.getMerchantId(),
                entity.getEnvironment(),
                entity.getStatus(),
                entity.getKeyId(),
                entity.getSecretHash(),
                entity.getSecretSuffix(),
                entity.getCreatedAt(),
                entity.getLastUsedAt(),
                entity.getRevokedAt()
        );
        domain.setVersion(entity.getVersion());
        return domain;
    }
}
