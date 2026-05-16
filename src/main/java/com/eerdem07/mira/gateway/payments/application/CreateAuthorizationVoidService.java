package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.AuthorizationVoidCannotBeCreatedException;
import com.eerdem07.mira.gateway.payments.application.exception.PaymentAttemptNotFoundException;
import com.eerdem07.mira.gateway.payments.application.exception.PaymentIntentNotFoundException;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateAuthorizationVoidCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateAuthorizationVoidResult;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateAuthorizationVoidUseCase;
import com.eerdem07.mira.gateway.payments.application.port.out.AuthorizationVoidRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.CaptureRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAttemptPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentProcessorPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentVoidRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentVoidResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentVoidStatus;
import com.eerdem07.mira.gateway.payments.domain.AuthorizationVoid;
import com.eerdem07.mira.gateway.payments.domain.AuthorizationVoidStatus;
import com.eerdem07.mira.gateway.payments.domain.CaptureMethod;
import com.eerdem07.mira.gateway.payments.domain.CaptureStatus;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttempt;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttemptStatus;
import com.eerdem07.mira.gateway.payments.domain.PaymentFailureCode;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

@Service
public class CreateAuthorizationVoidService implements CreateAuthorizationVoidUseCase {

    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort;
    private final PaymentAttemptPort paymentAttemptPort;
    private final AuthorizationVoidRepositoryPort authorizationVoidRepositoryPort;
    private final CaptureRepositoryPort captureRepositoryPort;
    private final PaymentProcessorPort paymentProcessorPort;
    private final Clock clock;

    public CreateAuthorizationVoidService(PaymentIntentRepositoryPort paymentIntentRepositoryPort,
                                          PaymentAttemptPort paymentAttemptPort,
                                          AuthorizationVoidRepositoryPort authorizationVoidRepositoryPort,
                                          CaptureRepositoryPort captureRepositoryPort,
                                          PaymentProcessorPort paymentProcessorPort,
                                          Clock clock) {
        this.paymentIntentRepositoryPort = paymentIntentRepositoryPort;
        this.paymentAttemptPort = paymentAttemptPort;
        this.authorizationVoidRepositoryPort = authorizationVoidRepositoryPort;
        this.captureRepositoryPort = captureRepositoryPort;
        this.paymentProcessorPort = paymentProcessorPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CreateAuthorizationVoidResult execute(CreateAuthorizationVoidCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        PaymentIntent paymentIntent = paymentIntentRepositoryPort.findById(command.paymentIntentId())
                .orElseThrow(() -> new PaymentIntentNotFoundException(command.paymentIntentId()));
        ensurePaymentIntentBelongsToMerchant(paymentIntent, command.merchantId());

        UUID authorizedPaymentAttemptId = paymentIntent.getAuthorizedPaymentAttemptId();
        if (authorizedPaymentAttemptId == null) {
            throw new AuthorizationVoidCannotBeCreatedException("Payment intent does not have an authorized payment attempt.");
        }

        PaymentAttempt paymentAttempt = paymentAttemptPort.findById(authorizedPaymentAttemptId)
                .orElseThrow(() -> new PaymentAttemptNotFoundException(authorizedPaymentAttemptId));

        Instant now = Instant.now(clock);
        Currency currency = resolveCurrency(command.currency());
        validateVoidRequest(paymentIntent, paymentAttempt, command.amount(), currency, now);

        AuthorizationVoid authorizationVoid = AuthorizationVoid.create(
                UUID.randomUUID(),
                paymentIntent.getId(),
                paymentAttempt.getId(),
                command.amount(),
                currency,
                paymentAttempt.getPosProvider(),
                now
        );
        authorizationVoid.markProcessing(now);

        PaymentVoidResult voidResult = paymentProcessorPort.voidAuthorization(
                toVoidRequest(paymentIntent, paymentAttempt, authorizationVoid)
        );
        applyVoidResult(paymentIntent, paymentAttempt, authorizationVoid, voidResult, now);

        AuthorizationVoid savedAuthorizationVoid = authorizationVoidRepositoryPort.save(authorizationVoid);
        paymentAttemptPort.save(paymentAttempt);
        paymentIntentRepositoryPort.save(paymentIntent);

        return new CreateAuthorizationVoidResult(
                savedAuthorizationVoid.getId(),
                savedAuthorizationVoid.getPaymentIntentId(),
                savedAuthorizationVoid.getPaymentAttemptId(),
                savedAuthorizationVoid.getStatus(),
                savedAuthorizationVoid.getAmount(),
                savedAuthorizationVoid.getCurrency().getCurrencyCode(),
                savedAuthorizationVoid.getPosProvider(),
                savedAuthorizationVoid.getPosVoidId(),
                savedAuthorizationVoid.getPosReference(),
                savedAuthorizationVoid.getPosResponseCode(),
                savedAuthorizationVoid.getPosResponseMessage(),
                savedAuthorizationVoid.getFailureCode(),
                savedAuthorizationVoid.getFailureMessage(),
                savedAuthorizationVoid.getCreatedAt(),
                savedAuthorizationVoid.getUpdatedAt(),
                savedAuthorizationVoid.getSucceededAt(),
                savedAuthorizationVoid.getFailedAt()
        );
    }

    private PaymentVoidRequest toVoidRequest(PaymentIntent paymentIntent,
                                             PaymentAttempt paymentAttempt,
                                             AuthorizationVoid authorizationVoid) {
        return new PaymentVoidRequest(
                paymentIntent.getId(),
                authorizationVoid.getId(),
                paymentAttempt.getId(),
                paymentIntent.getMerchantId(),
                authorizationVoid.getAmount(),
                authorizationVoid.getCurrency().getCurrencyCode(),
                resolveOrderId(paymentIntent),
                paymentAttempt.getPosTransactionId(),
                paymentAttempt.getPosAuthCode(),
                paymentAttempt.getPosReference()
        );
    }

    private void applyVoidResult(PaymentIntent paymentIntent,
                                 PaymentAttempt paymentAttempt,
                                 AuthorizationVoid authorizationVoid,
                                 PaymentVoidResult voidResult,
                                 Instant now) {
        if (voidResult.status() == PaymentVoidStatus.SUCCEEDED) {
            authorizationVoid.markSucceeded(
                    voidResult.posVoidId(),
                    voidResult.posReference(),
                    voidResult.responseCode(),
                    voidResult.responseMessage(),
                    now
            );
            paymentAttempt.voidAuthorization(now);
            paymentIntent.cancel(now);
            return;
        }

        authorizationVoid.markFailed(
                resolveFailureCode(voidResult),
                resolveFailureMessage(voidResult),
                voidResult.responseCode(),
                voidResult.responseMessage(),
                now
        );
    }

    private void ensurePaymentIntentBelongsToMerchant(PaymentIntent paymentIntent, UUID merchantId) {
        if (!paymentIntent.getMerchantId().equals(merchantId)) {
            throw new PaymentIntentNotFoundException(paymentIntent.getId());
        }
    }

    private void validateVoidRequest(PaymentIntent paymentIntent,
                                     PaymentAttempt paymentAttempt,
                                     BigDecimal amount,
                                     Currency currency,
                                     Instant now) {
        if (paymentIntent.getStatus() != PaymentIntentStatus.REQUIRES_CAPTURE) {
            throw new AuthorizationVoidCannotBeCreatedException(
                    "Payment intent must be REQUIRES_CAPTURE to void an authorization."
            );
        }

        if (paymentIntent.getCaptureMethod() != CaptureMethod.MANUAL) {
            throw new AuthorizationVoidCannotBeCreatedException("Only MANUAL payment intents can be voided separately.");
        }

        if (!paymentIntent.getId().equals(paymentAttempt.getPaymentIntentId())) {
            throw new AuthorizationVoidCannotBeCreatedException("Payment attempt does not belong to payment intent.");
        }

        if (!paymentAttempt.getId().equals(paymentIntent.getAuthorizedPaymentAttemptId())) {
            throw new AuthorizationVoidCannotBeCreatedException("Only latest authorized payment attempt can be voided.");
        }

        if (paymentAttempt.getStatus() != PaymentAttemptStatus.AUTHORIZED) {
            throw new AuthorizationVoidCannotBeCreatedException("Payment attempt must be AUTHORIZED to create an authorization void.");
        }

        if (paymentAttempt.getAuthorizationExpiresAt() == null || !paymentAttempt.getAuthorizationExpiresAt().isAfter(now)) {
            throw new AuthorizationVoidCannotBeCreatedException("Payment attempt authorization has expired.");
        }

        if (authorizationVoidRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntent.getId(), AuthorizationVoidStatus.CREATED)
                || authorizationVoidRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntent.getId(), AuthorizationVoidStatus.PROCESSING)) {
            throw new AuthorizationVoidCannotBeCreatedException("Payment intent already has an active authorization void.");
        }

        if (captureRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntent.getId(), CaptureStatus.CREATED)
                || captureRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntent.getId(), CaptureStatus.PROCESSING)
                || captureRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntent.getId(), CaptureStatus.SUCCEEDED)) {
            throw new AuthorizationVoidCannotBeCreatedException("Payment intent already has a capture and cannot be voided.");
        }

        validateAmount(paymentIntent, amount);
        validateCurrency(paymentIntent, currency);
        validatePosProvider(paymentAttempt);
        validateAuthorizationPosMetadata(paymentAttempt);
    }

    private void validateAmount(PaymentIntent paymentIntent, BigDecimal amount) {
        if (amount == null) {
            throw new AuthorizationVoidCannotBeCreatedException("Authorization void amount must not be null.");
        }

        if (paymentIntent.getAmount().compareTo(amount) != 0) {
            throw new AuthorizationVoidCannotBeCreatedException(
                    "Authorization void amount must equal payment intent amount."
            );
        }
    }

    private void validateCurrency(PaymentIntent paymentIntent, Currency currency) {
        if (!paymentIntent.getCurrency().equals(currency)) {
            throw new AuthorizationVoidCannotBeCreatedException("Authorization void currency must equal payment intent currency.");
        }
    }

    private void validatePosProvider(PaymentAttempt paymentAttempt) {
        if (paymentAttempt.getPosProvider() == null || paymentAttempt.getPosProvider().isBlank()) {
            throw new AuthorizationVoidCannotBeCreatedException("Authorized payment attempt does not have a POS provider.");
        }
    }

    private void validateAuthorizationPosMetadata(PaymentAttempt paymentAttempt) {
        if (paymentAttempt.getPosTransactionId() == null || paymentAttempt.getPosTransactionId().isBlank()) {
            throw new AuthorizationVoidCannotBeCreatedException("Authorized payment attempt does not have a POS transaction id.");
        }
        if (paymentAttempt.getPosAuthCode() == null || paymentAttempt.getPosAuthCode().isBlank()) {
            throw new AuthorizationVoidCannotBeCreatedException("Authorized payment attempt does not have a POS authorization code.");
        }
        if (paymentAttempt.getPosReference() == null || paymentAttempt.getPosReference().isBlank()) {
            throw new AuthorizationVoidCannotBeCreatedException("Authorized payment attempt does not have a POS reference.");
        }
    }

    private String resolveOrderId(PaymentIntent paymentIntent) {
        if (paymentIntent.getMerchantReference() == null || paymentIntent.getMerchantReference().isBlank()) {
            return paymentIntent.getId().toString();
        }
        return paymentIntent.getMerchantReference();
    }

    private String resolveFailureCode(PaymentVoidResult voidResult) {
        if (voidResult.failureCode() != null && !voidResult.failureCode().isBlank()) {
            return voidResult.failureCode();
        }
        if (voidResult.responseCode() != null && !voidResult.responseCode().isBlank()) {
            return voidResult.responseCode();
        }
        return PaymentFailureCode.UNKNOWN_POS_ERROR.name();
    }

    private String resolveFailureMessage(PaymentVoidResult voidResult) {
        if (voidResult.failureMessage() != null && !voidResult.failureMessage().isBlank()) {
            return voidResult.failureMessage();
        }
        if (voidResult.responseMessage() != null && !voidResult.responseMessage().isBlank()) {
            return voidResult.responseMessage();
        }
        return "Payment authorization void failed";
    }

    private Currency resolveCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new AuthorizationVoidCannotBeCreatedException("Authorization void currency must not be blank.");
        }

        return Currency.getInstance(currency);
    }
}
