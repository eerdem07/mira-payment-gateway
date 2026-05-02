package com.eerdem07.mira.gateway.payments.application.port.out;

import com.eerdem07.mira.gateway.payments.domain.CheckoutSession;

import java.util.Optional;

public interface CheckoutSessionRepositoryPort {
    CheckoutSession save(CheckoutSession checkoutSession);
    Optional<CheckoutSession> findByToken(String token);
}
