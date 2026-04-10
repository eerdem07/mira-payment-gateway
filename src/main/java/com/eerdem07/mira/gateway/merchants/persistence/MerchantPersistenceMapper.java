package com.eerdem07.mira.gateway.merchants.persistence;

import com.eerdem07.mira.gateway.merchants.domain.Merchant;

public final class MerchantPersistenceMapper {

    private MerchantPersistenceMapper() {
    }

    public static MerchantJpaEntity toEntity(Merchant domain) {
        return new MerchantJpaEntity(
                domain.getMerchantId(),
                domain.getEmail(),
                domain.getPasswordHash(),
                domain.getLegalName(),
                domain.getStatus(),
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
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getActivatedAt(),
                entity.getSuspendedAt()
        );
    }

//    private static MerchantStatus toJpaStatus(MerchantStatus status) {
//        return MerchantStatus.valueOf(status.name());
//    }
//
//    private static MerchantStatus toDomainStatus(MerchantStatus status) {
//        return MerchantStatus.valueOf(status.name());
//    }
}

