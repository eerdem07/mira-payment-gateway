package com.eerdem07.mira.gateway.payments.application.port.out;

import com.eerdem07.mira.gateway.payments.domain.AuthorizationVoid;
import com.eerdem07.mira.gateway.payments.domain.AuthorizationVoidStatus;

import java.util.Optional;
import java.util.UUID;

public interface AuthorizationVoidRepositoryPort {

    AuthorizationVoid save(AuthorizationVoid authorizationVoid);

    Optional<AuthorizationVoid> findById(UUID id);

    boolean existsByPaymentIntentIdAndStatus(UUID paymentIntentId, AuthorizationVoidStatus status);
}
