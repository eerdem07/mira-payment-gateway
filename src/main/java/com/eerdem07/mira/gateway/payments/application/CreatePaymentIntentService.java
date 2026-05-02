package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.port.in.CreatePaymentIntentCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreatePaymentIntentResult;
import com.eerdem07.mira.gateway.payments.application.port.in.CreatePaymentIntentUseCase;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Service
public class CreatePaymentIntentService implements CreatePaymentIntentUseCase {
    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort;
    private final Clock clock;

    public CreatePaymentIntentService(PaymentIntentRepositoryPort paymentIntentRepositoryPort, Clock clock) {
        this.paymentIntentRepositoryPort = paymentIntentRepositoryPort;
        this.clock = clock;
    }

    @Transactional
    @Override
    public CreatePaymentIntentResult execute(CreatePaymentIntentCommand command) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plusSeconds(1800); // 30 minutes expiration

        PaymentIntent paymentIntent = PaymentIntent.create(
                UUID.randomUUID(),
                command.merchantId(),
                command.amount(),
                Currency.getInstance(command.currency()),
                command.merchantReference(),
                command.description(),
                now,
                expiresAt
        );

        PaymentIntent savedIntent = paymentIntentRepositoryPort.save(paymentIntent);

        return new CreatePaymentIntentResult(
                savedIntent.getId().toString(),
                savedIntent.getStatus().name(),
                savedIntent.getAmount(),
                savedIntent.getCurrency().getCurrencyCode(),
                savedIntent.getMerchantReference(),
                savedIntent.getDescription(),
                savedIntent.getExpiresAt(),
                savedIntent.getCreatedAt()
        );
    }
}
