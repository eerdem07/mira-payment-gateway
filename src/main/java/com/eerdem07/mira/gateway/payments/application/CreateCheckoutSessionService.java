package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.PaymentIntentNotFoundException;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateCheckoutSessionCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateCheckoutSessionResult;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateCheckoutSessionUseCase;
import com.eerdem07.mira.gateway.payments.application.port.out.CheckoutSessionRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.domain.CheckoutSession;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class CreateCheckoutSessionService implements CreateCheckoutSessionUseCase {

    private final CheckoutSessionRepositoryPort checkoutSessionRepositoryPort;
    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort;
    private final String checkoutBaseUrl;
    private final Clock clock;

    public CreateCheckoutSessionService(CheckoutSessionRepositoryPort checkoutSessionRepositoryPort, 
                                        PaymentIntentRepositoryPort paymentIntentRepositoryPort,
                                        @Value("${mira.checkout.base-url}") String checkoutBaseUrl,
                                        Clock clock) {
        this.checkoutSessionRepositoryPort = checkoutSessionRepositoryPort;
        this.paymentIntentRepositoryPort = paymentIntentRepositoryPort;
        this.checkoutBaseUrl = checkoutBaseUrl;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CreateCheckoutSessionResult execute(CreateCheckoutSessionCommand command) {
        
        PaymentIntent paymentIntent = paymentIntentRepositoryPort.findById(command.paymentIntentId())
                .orElseThrow(() -> new PaymentIntentNotFoundException(command.paymentIntentId()));

        UUID sessionId = UUID.randomUUID();
        String token = "cs_test_" + UUID.randomUUID().toString().replace("-", ""); 
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(30, ChronoUnit.MINUTES); 

        CheckoutSession checkoutSession = CheckoutSession.create(
                sessionId,
                paymentIntent.getId(),
                token,
                command.returnUrl(),
                command.cancelUrl(),
                expiresAt,
                now
        );

        CheckoutSession savedSession = checkoutSessionRepositoryPort.save(checkoutSession);

        return new CreateCheckoutSessionResult(
                savedSession.getId(),
                savedSession.getPaymentIntentId(),
                savedSession.getToken(),
                checkoutBaseUrl + "/" + savedSession.getToken(),
                savedSession.getStatus(),
                savedSession.getExpiresAt(),
                savedSession.getCreatedAt()
        );
    }
}
