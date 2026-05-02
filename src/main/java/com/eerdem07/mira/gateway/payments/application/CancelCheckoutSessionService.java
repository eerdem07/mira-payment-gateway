package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.CheckoutSessionNotFoundException;
import com.eerdem07.mira.gateway.payments.application.exception.PaymentIntentNotFoundException;
import com.eerdem07.mira.gateway.payments.application.port.in.CancelCheckoutSessionCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CancelCheckoutSessionResult;
import com.eerdem07.mira.gateway.payments.application.port.in.CancelCheckoutSessionUseCase;
import com.eerdem07.mira.gateway.payments.application.port.out.CheckoutSessionRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.domain.CheckoutSession;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class CancelCheckoutSessionService implements CancelCheckoutSessionUseCase {
    private final CheckoutSessionRepositoryPort checkoutSessionRepositoryPort;
    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort;
    private final Clock clock;


    public CancelCheckoutSessionService(CheckoutSessionRepositoryPort checkoutSessionRepositoryPort,
                                        PaymentIntentRepositoryPort paymentIntentRepositoryPort,
                                        Clock clock) {
        this.checkoutSessionRepositoryPort = checkoutSessionRepositoryPort;
        this.paymentIntentRepositoryPort = paymentIntentRepositoryPort;
        this.clock = clock;
    }

    @Transactional
    @Override
    public CancelCheckoutSessionResult execute(CancelCheckoutSessionCommand command) {
        CheckoutSession checkoutSession = checkoutSessionRepositoryPort.findByToken(command.token())
                .orElseThrow(() -> new CheckoutSessionNotFoundException(command.token()));

        PaymentIntent paymentIntent = paymentIntentRepositoryPort.findById(checkoutSession.getPaymentIntentId())
                .orElseThrow(() -> new PaymentIntentNotFoundException(checkoutSession.getPaymentIntentId()));

        Instant now = Instant.now(clock);
        checkoutSession.cancel(now);
        paymentIntent.cancel(now);

        checkoutSessionRepositoryPort.save(checkoutSession);
        paymentIntentRepositoryPort.save(paymentIntent);

        return new CancelCheckoutSessionResult();
    }
}
