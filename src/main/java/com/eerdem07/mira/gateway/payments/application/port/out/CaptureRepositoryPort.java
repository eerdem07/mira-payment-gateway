package com.eerdem07.mira.gateway.payments.application.port.out;

import com.eerdem07.mira.gateway.payments.domain.Capture;
import com.eerdem07.mira.gateway.payments.domain.CaptureStatus;

import java.util.Optional;
import java.util.UUID;

public interface CaptureRepositoryPort {

    Capture save(Capture capture);

    Optional<Capture> findById(UUID id);

    boolean existsByPaymentIntentIdAndStatus(UUID paymentIntentId, CaptureStatus status);
}
