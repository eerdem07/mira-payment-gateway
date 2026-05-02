package com.eerdem07.mira.gateway.payments.processor;

import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAuthorizationRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAuthorizationResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAuthorizationStatus;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentProcessorPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SimulatedPaymentProcessorAdapter implements PaymentProcessorPort {

    private static final String DECLINED_CARD_NUMBER = "4000000000000002";

    @Override
    public PaymentAuthorizationResult authorize(PaymentAuthorizationRequest request) {
        if (hasMissingCardDetails(request)) {
            return new PaymentAuthorizationResult(
                    PaymentAuthorizationStatus.ERROR,
                    null,
                    null,
                    "invalid_card_details",
                    "Card details are incomplete."
            );
        }

        if (DECLINED_CARD_NUMBER.equals(request.cardNumber())) {
            return new PaymentAuthorizationResult(
                    PaymentAuthorizationStatus.DECLINED,
                    null,
                    null,
                    "card_declined",
                    "The card was declined."
            );
        }

        return new PaymentAuthorizationResult(
                PaymentAuthorizationStatus.AUTHORIZED,
                UUID.randomUUID().toString(),
                "AUTH-" + request.paymentIntentId().toString().substring(0, 8),
                null,
                null
        );
    }

    private boolean hasMissingCardDetails(PaymentAuthorizationRequest request) {
        return isBlank(request.cardNumber())
                || isBlank(request.expiryMonth())
                || isBlank(request.expiryYear())
                || isBlank(request.cvc())
                || isBlank(request.cardHolderName());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
