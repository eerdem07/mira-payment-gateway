package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.CheckoutSessionNotFoundException;
import com.eerdem07.mira.gateway.payments.application.exception.PaymentIntentNotFoundException;
import com.eerdem07.mira.gateway.payments.application.port.in.GetCheckoutSessionQuery;
import com.eerdem07.mira.gateway.payments.application.port.in.GetCheckoutSessionResult;
import com.eerdem07.mira.gateway.payments.application.port.in.GetCheckoutSessionUseCase;
import com.eerdem07.mira.gateway.payments.application.port.out.CheckoutSessionRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.domain.CheckoutSession;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetCheckoutSessionService implements GetCheckoutSessionUseCase {

    private final CheckoutSessionRepositoryPort checkoutSessionRepositoryPort;
    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort;

    public GetCheckoutSessionService(CheckoutSessionRepositoryPort checkoutSessionRepositoryPort, PaymentIntentRepositoryPort paymentIntentRepositoryPort) {
        this.checkoutSessionRepositoryPort = checkoutSessionRepositoryPort;
        this.paymentIntentRepositoryPort = paymentIntentRepositoryPort;
    }

    @Override
    public GetCheckoutSessionResult getCheckoutSession(GetCheckoutSessionQuery query) {
        CheckoutSession checkoutSession = checkoutSessionRepositoryPort.findByToken(query.token())
                .orElseThrow(() -> new CheckoutSessionNotFoundException(query.token()));

        PaymentIntent paymentIntent = paymentIntentRepositoryPort.findById(checkoutSession.getPaymentIntentId())
                .orElseThrow(() -> new PaymentIntentNotFoundException(checkoutSession.getPaymentIntentId()));

        return new GetCheckoutSessionResult(
                checkoutSession.getId().toString(),
                checkoutSession.getStatus().name(),
                checkoutSession.getExpiresAt(),
                checkoutSession.getReturnUrl(),
                checkoutSession.getCancelUrl(),
                new GetCheckoutSessionResult.PaymentDetails(
                        paymentIntent.getAmount(),
                        paymentIntent.getCurrency().getCurrencyCode(),
                        paymentIntent.getDescription()
                )
        );
    }
}
