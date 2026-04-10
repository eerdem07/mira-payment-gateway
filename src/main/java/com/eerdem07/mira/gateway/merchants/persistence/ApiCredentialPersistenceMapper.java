package com.eerdem07.mira.gateway.merchants.persistence;

import com.eerdem07.mira.gateway.merchants.domain.ApiCredential;

public final class ApiCredentialPersistenceMapper {
    private ApiCredentialPersistenceMapper() {
    }

    public static ApiCredentialJpaEntity toEntity(ApiCredential domain) {
        return new ApiCredentialJpaEntity(
                domain.getCredentialId(),
                domain.getMerchantId(),
                domain.getKeyId(),
                domain.getSecretHash(),
                domain.getSecretPrefix(),
                domain.getApiCredentialEnvironment(),
                domain.getApiCredentialStatus(),
                domain.getCreatedAt(),
                domain.getLastUsedAt(),
                domain.getRevokedAt()
        );
    }

    public static ApiCredential toDomain(ApiCredentialJpaEntity entity) {
        return ApiCredential.restore(entity.getCredentialId(),
                entity.getMerchantId(),
                entity.getEnvironment(),
                entity.getStatus(),
                entity.getKeyId(),
                entity.getSecretHash(),
                entity.getSecretPrefix(),
                entity.getCreatedAt(),
                entity.getLastUsedAt(),
                entity.getRevokedAt());
    }
}
