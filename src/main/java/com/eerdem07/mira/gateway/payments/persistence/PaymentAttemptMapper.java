package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.domain.PaymentAttempt;

import java.util.Currency;

public final class PaymentAttemptMapper {

    private PaymentAttemptMapper() {
    }

    public static PaymentAttemptJpaEntity toEntity(PaymentAttempt domain) {
        if (domain == null) {
            return null;
        }

        return new PaymentAttemptJpaEntity(
                domain.getId(),
                domain.getPaymentIntentId(),
                domain.getCheckoutSessionId(),
                domain.getStatus(),
                domain.getAmount(),
                domain.getCurrency().getCurrencyCode(),
                domain.getCardBrand(),
                domain.getCardLast4(),
                domain.getPosProvider(),
                domain.getPosTransactionId(),
                domain.getPosAuthCode(),
                domain.getPosReference(),
                domain.getPosResponseCode(),
                domain.getPosResponseMessage(),
                domain.getDeclineCode(),
                domain.getDeclineMessage(),
                domain.getFailureCode(),
                domain.getFailureMessage(),
                domain.getThreeDsSessionId(),
                domain.getThreeDsFlow(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getProcessingStartedAt(),
                domain.getActionRequiredAt(),
                domain.getAuthorizedAt(),
                domain.getAuthorizationExpiresAt(),
                domain.getSucceededAt(),
                domain.getDeclinedAt(),
                domain.getFailedAt(),
                domain.getCanceledAt(),
                domain.getVoidedAt(),
                domain.getExpiredAt()
        );
    }

    public static PaymentAttempt toDomain(PaymentAttemptJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return PaymentAttempt.restore(
                entity.getId(),
                entity.getPaymentIntentId(),
                entity.getCheckoutSessionId(),
                entity.getStatus(),
                entity.getAmount(),
                Currency.getInstance(entity.getCurrency()),
                entity.getCardBrand(),
                entity.getCardLast4(),
                entity.getPosProvider(),
                entity.getPosTransactionId(),
                entity.getPosAuthCode(),
                entity.getPosReference(),
                entity.getPosResponseCode(),
                entity.getPosResponseMessage(),
                entity.getDeclineCode(),
                entity.getDeclineMessage(),
                entity.getFailureCode(),
                entity.getFailureMessage(),
                entity.getThreeDsSessionId(),
                entity.getThreeDsFlow(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getProcessingStartedAt(),
                entity.getActionRequiredAt(),
                entity.getAuthorizedAt(),
                entity.getAuthorizationExpiresAt(),
                entity.getSucceededAt(),
                entity.getDeclinedAt(),
                entity.getFailedAt(),
                entity.getCanceledAt(),
                entity.getVoidedAt(),
                entity.getExpiredAt()
        );
    }
}
