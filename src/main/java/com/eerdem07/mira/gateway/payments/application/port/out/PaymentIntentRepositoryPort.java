package com.eerdem07.mira.gateway.payments.application.port.out;

import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;

import java.util.Optional;
import java.util.UUID;

public interface PaymentIntentRepositoryPort {
    PaymentIntent save(PaymentIntent intent);
    Optional<PaymentIntent> findById(UUID id);
}
