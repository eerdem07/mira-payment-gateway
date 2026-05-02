package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.CheckoutSessionNotFoundException;
import com.eerdem07.mira.gateway.payments.application.exception.PaymentIntentNotFoundException;
import com.eerdem07.mira.gateway.payments.application.port.in.SubmitCheckoutSessionCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.SubmitCheckoutSessionResult;
import com.eerdem07.mira.gateway.payments.application.port.in.SubmitCheckoutSessionUseCase;
import com.eerdem07.mira.gateway.payments.application.port.out.CheckoutSessionRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAuthorizationRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAuthorizationResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentProcessorPort;
import com.eerdem07.mira.gateway.payments.domain.CheckoutSession;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class SubmitCheckoutSessionService implements SubmitCheckoutSessionUseCase {

    private final CheckoutSessionRepositoryPort checkoutSessionRepositoryPort;
    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort;
    private final PaymentProcessorPort paymentProcessorPort;
    private final Clock clock;

    public SubmitCheckoutSessionService(CheckoutSessionRepositoryPort checkoutSessionRepositoryPort,
                                        PaymentIntentRepositoryPort paymentIntentRepositoryPort,
                                        PaymentProcessorPort paymentProcessorPort,
                                        Clock clock) {
        this.checkoutSessionRepositoryPort = checkoutSessionRepositoryPort;
        this.paymentIntentRepositoryPort = paymentIntentRepositoryPort;
        this.paymentProcessorPort = paymentProcessorPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public SubmitCheckoutSessionResult execute(SubmitCheckoutSessionCommand command) {
        CheckoutSession checkoutSession = checkoutSessionRepositoryPort.findByToken(command.token())
                .orElseThrow(() -> new CheckoutSessionNotFoundException(command.token()));

        PaymentIntent paymentIntent = paymentIntentRepositoryPort.findById(checkoutSession.getPaymentIntentId())
                .orElseThrow(() -> new PaymentIntentNotFoundException(checkoutSession.getPaymentIntentId()));

        Instant now = Instant.now(clock);
        checkoutSession.validateOpenAt(now);
        paymentIntent.attachPaymentMethod(now);
        paymentIntent.validateConfirmable();
        paymentIntent.markProcessing(now);

        PaymentAuthorizationResult authorizationResult = paymentProcessorPort.authorize(toAuthorizationRequest(command, paymentIntent));
        applyAuthorizationResult(paymentIntent, authorizationResult, now);
        checkoutSession.submit(now);

        CheckoutSession savedCheckoutSession = checkoutSessionRepositoryPort.save(checkoutSession);
        PaymentIntent savedPaymentIntent = paymentIntentRepositoryPort.save(paymentIntent);

        return new SubmitCheckoutSessionResult(
                savedCheckoutSession.getId(),
                savedPaymentIntent.getId(),
                savedCheckoutSession.getStatus(),
                savedPaymentIntent.getStatus(),
                savedCheckoutSession.getReturnUrl(),
                savedPaymentIntent.getFailureCode(),
                savedPaymentIntent.getFailureMessage()
        );
    }

    private PaymentAuthorizationRequest toAuthorizationRequest(SubmitCheckoutSessionCommand command, PaymentIntent paymentIntent) {
        return new PaymentAuthorizationRequest(
                paymentIntent.getId(),
                paymentIntent.getMerchantId(),
                paymentIntent.getAmount(),
                paymentIntent.getCurrency().getCurrencyCode(),
                paymentIntent.getMerchantReference(),
                command.cardNumber(),
                command.expiryMonth(),
                command.expiryYear(),
                command.cvc(),
                command.cardHolderName()
        );
    }

    private void applyAuthorizationResult(PaymentIntent paymentIntent,
                                          PaymentAuthorizationResult authorizationResult,
                                          Instant now) {
        switch (authorizationResult.status()) {
            case AUTHORIZED -> paymentIntent.markSucceeded(now);
            case PENDING -> paymentIntent.markProcessing(now);
            case DECLINED, ERROR -> paymentIntent.markFailed(
                    authorizationResult.failureCode(),
                    authorizationResult.failureMessage(),
                    now
            );
        }
    }
}
