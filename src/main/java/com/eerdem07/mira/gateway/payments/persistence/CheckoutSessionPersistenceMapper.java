package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.domain.CheckoutSession;

public final class CheckoutSessionPersistenceMapper {

    private CheckoutSessionPersistenceMapper() {
    }

    public static CheckoutSessionJpaEntity toEntity(CheckoutSession domain) {
        if (domain == null) {
            return null;
        }

        return new CheckoutSessionJpaEntity(
                domain.getId(),
                domain.getPaymentIntentId(),
                domain.getToken(),
                domain.getStatus(),
                domain.getReturnUrl(),
                domain.getCancelUrl(),
                domain.getExpiresAt(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getSubmittedAt(),
                domain.getCanceledAt()
        );
    }

    public static CheckoutSession toDomain(CheckoutSessionJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return CheckoutSession.restore(
                entity.getId(),
                entity.getPaymentIntentId(),
                entity.getToken(),
                entity.getStatus(),
                entity.getReturnUrl(),
                entity.getCancelUrl(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getSubmittedAt(),
                entity.getCanceledAt()
        );
    }
}
