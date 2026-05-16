package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.PaymentIntentNotFoundException;
import com.eerdem07.mira.gateway.payments.application.port.in.CancelPaymentIntentCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CancelPaymentIntentResult;
import com.eerdem07.mira.gateway.payments.application.port.in.CancelPaymentIntentUseCase;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class CancelPaymentIntentService implements CancelPaymentIntentUseCase {

    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort;
    private final Clock clock;

    public CancelPaymentIntentService(PaymentIntentRepositoryPort paymentIntentRepositoryPort, Clock clock) {
        this.paymentIntentRepositoryPort = paymentIntentRepositoryPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CancelPaymentIntentResult execute(CancelPaymentIntentCommand command) {
        PaymentIntent paymentIntent = paymentIntentRepositoryPort
                .findByIdAndMerchantId(command.paymentIntentId(), command.merchantId())
                .orElseThrow(() -> new PaymentIntentNotFoundException(command.paymentIntentId()));

        Instant now = Instant.now(clock);
        paymentIntent.cancel(now);

        PaymentIntent saved = paymentIntentRepositoryPort.save(paymentIntent);
        return new CancelPaymentIntentResult(saved.getId(), saved.getStatus());
    }
}
