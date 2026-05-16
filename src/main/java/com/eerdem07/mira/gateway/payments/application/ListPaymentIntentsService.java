package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.port.in.GetPaymentIntentResult;
import com.eerdem07.mira.gateway.payments.application.port.in.ListPaymentIntentsQuery;
import com.eerdem07.mira.gateway.payments.application.port.in.ListPaymentIntentsResult;
import com.eerdem07.mira.gateway.payments.application.port.in.ListPaymentIntentsUseCase;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListPaymentIntentsService implements ListPaymentIntentsUseCase {

    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort;

    public ListPaymentIntentsService(PaymentIntentRepositoryPort paymentIntentRepositoryPort) {
        this.paymentIntentRepositoryPort = paymentIntentRepositoryPort;
    }

    @Override
    public ListPaymentIntentsResult execute(ListPaymentIntentsQuery query) {
        List<GetPaymentIntentResult> items = paymentIntentRepositoryPort
                .findAllByMerchantId(query.merchantId())
                .stream()
                .map(this::toResult)
                .toList();

        return new ListPaymentIntentsResult(items);
    }

    private GetPaymentIntentResult toResult(PaymentIntent intent) {
        return new GetPaymentIntentResult(
                intent.getId().toString(),
                intent.getStatus().name(),
                intent.getAmount(),
                intent.getCurrency().getCurrencyCode(),
                intent.getCaptureMethod(),
                intent.getMerchantReference(),
                intent.getDescription(),
                intent.getExpiresAt(),
                intent.getCreatedAt()
        );
    }
}
