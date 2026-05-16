package com.eerdem07.mira.gateway.payments.application.port.out;

import com.eerdem07.mira.gateway.payments.domain.Refund;
import com.eerdem07.mira.gateway.payments.domain.RefundStatus;

import java.util.Optional;
import java.util.UUID;

public interface RefundRepositoryPort {

    Refund save(Refund refund);

    Optional<Refund> findById(UUID id);

    boolean existsByPaymentIntentIdAndStatus(UUID paymentIntentId, RefundStatus status);
}
