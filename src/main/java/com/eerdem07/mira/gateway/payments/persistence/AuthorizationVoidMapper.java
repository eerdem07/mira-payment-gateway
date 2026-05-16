package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.domain.AuthorizationVoid;

import java.util.Currency;

public final class AuthorizationVoidMapper {

    private AuthorizationVoidMapper() {
    }

    public static AuthorizationVoidJpaEntity toEntity(AuthorizationVoid domain) {
        if (domain == null) {
            return null;
        }

        return new AuthorizationVoidJpaEntity(
                domain.getId(),
                domain.getPaymentIntentId(),
                domain.getPaymentAttemptId(),
                domain.getStatus(),
                domain.getAmount(),
                domain.getCurrency().getCurrencyCode(),
                domain.getPosProvider(),
                domain.getPosVoidId(),
                domain.getPosReference(),
                domain.getPosResponseCode(),
                domain.getPosResponseMessage(),
                domain.getFailureCode(),
                domain.getFailureMessage(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getProcessingStartedAt(),
                domain.getSucceededAt(),
                domain.getFailedAt(),
                domain.getCanceledAt()
        );
    }

    public static AuthorizationVoid toDomain(AuthorizationVoidJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return AuthorizationVoid.restore(
                entity.getId(),
                entity.getPaymentIntentId(),
                entity.getPaymentAttemptId(),
                entity.getStatus(),
                entity.getAmount(),
                Currency.getInstance(entity.getCurrency()),
                entity.getPosProvider(),
                entity.getPosVoidId(),
                entity.getPosReference(),
                entity.getPosResponseCode(),
                entity.getPosResponseMessage(),
                entity.getFailureCode(),
                entity.getFailureMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getProcessingStartedAt(),
                entity.getSucceededAt(),
                entity.getFailedAt(),
                entity.getCanceledAt()
        );
    }
}
