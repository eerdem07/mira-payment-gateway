package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;

public final class PaymentIntentPersistenceMapper {

    private PaymentIntentPersistenceMapper() {
    }

    public static PaymentIntentJpaEntity toEntity(PaymentIntent domain) {
        if (domain == null) {
            return null;
        }

        return new PaymentIntentJpaEntity(
                domain.getId(),
                domain.getMerchantId(),
                domain.getAmount(),
                domain.getCurrency(),
                domain.getCaptureMethod(),
                domain.getMerchantReference(),
                domain.getDescription(),
                domain.getStatus(),
                domain.getAttemptCount(),
                domain.getFailureCode(),
                domain.getFailureMessage(),
                domain.getExpiresAt(),
                domain.getAuthorizationExpiresAt(),
                domain.getAuthorizedPaymentAttemptId(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getSucceededAt(),
                domain.getCanceledAt()
        );
    }

    public static PaymentIntent toDomain(PaymentIntentJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return PaymentIntent.restore(
                entity.getId(),
                entity.getMerchantId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getCaptureMethod(),
                entity.getMerchantReference(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getAttemptCount(),
                entity.getFailureCode(),
                entity.getFailureMessage(),
                entity.getExpiresAt(),
                entity.getAuthorizationExpiresAt(),
                entity.getAuthorizedPaymentAttemptId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getSucceededAt(),
                entity.getCanceledAt()
        );
    }
}
