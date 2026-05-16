package com.eerdem07.mira.gateway.payments.application.port.out;

import com.eerdem07.mira.gateway.payments.domain.PaymentAttempt;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttemptStatus;

import java.util.Optional;
import java.util.UUID;

public interface PaymentAttemptPort {

    PaymentAttempt save(PaymentAttempt paymentAttempt);

    Optional<PaymentAttempt> findById(UUID id);

    Optional<PaymentAttempt> findLatestByPaymentIntentIdAndStatus(UUID paymentIntentId, PaymentAttemptStatus status);
}
