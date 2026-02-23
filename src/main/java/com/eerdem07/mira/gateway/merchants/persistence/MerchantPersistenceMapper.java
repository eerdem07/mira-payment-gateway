package com.eerdem07.mira.gateway.merchants.persistence;

import com.eerdem07.mira.gateway.merchants.domain.Merchant;
import com.eerdem07.mira.gateway.merchants.domain.MerchantStatus;

public final class MerchantPersistenceMapper {

    private MerchantPersistenceMapper() {
    }

    public static MerchantJpaEntity toEntity(Merchant domain) {
        return new MerchantJpaEntity(
                domain.getMerchantId(),
                domain.getEmail(),
                domain.getPasswordHash(),
                domain.getLegalName(),
                toJpaStatus(domain.getStatus()),
                domain.getCreatedAt(),
                domain.getActivatedAt()
                        .orElse(null),
                domain.getSuspendedAt()
                        .orElse(null)
        );
    }

    public static Merchant toDomain(MerchantJpaEntity entity) {
        return Merchant.restore(
                entity.getMerchantId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getLegalName(),
                toDomainStatus(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getActivatedAt(),
                entity.getSuspendedAt()
        );
    }

    private static MerchantStatusJpa toJpaStatus(MerchantStatus status) {
        return MerchantStatusJpa.valueOf(status.name());
    }

    private static MerchantStatus toDomainStatus(MerchantStatusJpa status) {
        return MerchantStatus.valueOf(status.name());
    }
}

