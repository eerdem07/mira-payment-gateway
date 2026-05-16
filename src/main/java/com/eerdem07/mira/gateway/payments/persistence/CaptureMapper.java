package com.eerdem07.mira.gateway.payments.persistence;

import com.eerdem07.mira.gateway.payments.domain.Capture;

import java.util.Currency;

public final class CaptureMapper {

    private CaptureMapper() {
    }

    public static CaptureJpaEntity toEntity(Capture domain) {
        if (domain == null) {
            return null;
        }

        return new CaptureJpaEntity(
                domain.getId(),
                domain.getPaymentIntentId(),
                domain.getPaymentAttemptId(),
                domain.getStatus(),
                domain.getAmount(),
                domain.getCurrency().getCurrencyCode(),
                domain.getPosProvider(),
                domain.getPosCaptureId(),
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

    public static Capture toDomain(CaptureJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Capture.restore(
                entity.getId(),
                entity.getPaymentIntentId(),
                entity.getPaymentAttemptId(),
                entity.getStatus(),
                entity.getAmount(),
                Currency.getInstance(entity.getCurrency()),
                entity.getPosProvider(),
                entity.getPosCaptureId(),
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
