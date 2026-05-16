package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.PaymentIntentNotFoundException;
import com.eerdem07.mira.gateway.payments.application.port.in.GetPaymentIntentQuery;
import com.eerdem07.mira.gateway.payments.application.port.in.GetPaymentIntentResult;
import com.eerdem07.mira.gateway.payments.application.port.in.GetPaymentIntentUseCase;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;
import org.springframework.stereotype.Service;

@Service
public class GetPaymentIntentService implements GetPaymentIntentUseCase {

    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort;

    public GetPaymentIntentService(PaymentIntentRepositoryPort paymentIntentRepositoryPort) {
        this.paymentIntentRepositoryPort = paymentIntentRepositoryPort;
    }

    @Override
    public GetPaymentIntentResult execute(GetPaymentIntentQuery query) {
        PaymentIntent paymentIntent = paymentIntentRepositoryPort.findById(query.id())
                .orElseThrow(() -> new PaymentIntentNotFoundException(query.id()));

        return new GetPaymentIntentResult(
                paymentIntent.getId().toString(),
                paymentIntent.getStatus().name(),
                paymentIntent.getAmount(),
                paymentIntent.getCurrency().getCurrencyCode(),
                paymentIntent.getCaptureMethod(),
                paymentIntent.getMerchantReference(),
                paymentIntent.getDescription(),
                paymentIntent.getExpiresAt(),
                paymentIntent.getCreatedAt()
        );
    }
}
