package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.CheckoutSessionNotFoundException;
import com.eerdem07.mira.gateway.payments.application.exception.PaymentIntentNotFoundException;
import com.eerdem07.mira.gateway.payments.application.port.in.SubmitCheckoutSessionCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.SubmitCheckoutSessionResult;
import com.eerdem07.mira.gateway.payments.application.port.in.SubmitCheckoutSessionUseCase;
import com.eerdem07.mira.gateway.payments.application.port.out.CheckoutSessionRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAttemptPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAuthorizationRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAuthorizationResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentProcessorPort;
import com.eerdem07.mira.gateway.payments.domain.CaptureMethod;
import com.eerdem07.mira.gateway.payments.domain.CheckoutSession;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttempt;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class SubmitCheckoutSessionService implements SubmitCheckoutSessionUseCase {

    private static final Duration AUTHORIZATION_WINDOW = Duration.ofDays(7);

    private final CheckoutSessionRepositoryPort checkoutSessionRepositoryPort;
    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort;
    private final PaymentAttemptPort paymentAttemptPort;
    private final PaymentProcessorPort paymentProcessorPort;
    private final Clock clock;

    public SubmitCheckoutSessionService(CheckoutSessionRepositoryPort checkoutSessionRepositoryPort,
                                        PaymentIntentRepositoryPort paymentIntentRepositoryPort,
                                        PaymentAttemptPort paymentAttemptPort,
                                        PaymentProcessorPort paymentProcessorPort,
                                        Clock clock) {
        this.checkoutSessionRepositoryPort = checkoutSessionRepositoryPort;
        this.paymentIntentRepositoryPort = paymentIntentRepositoryPort;
        this.paymentAttemptPort = paymentAttemptPort;
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
        paymentIntent.markProcessing(now);

        PaymentAttempt paymentAttempt = PaymentAttempt.create(
                UUID.randomUUID(),
                paymentIntent.getId(),
                checkoutSession.getId(),
                paymentIntent.getAmount(),
                paymentIntent.getCurrency(),
                resolveCardBrand(command.cardNumber()),
                resolveCardLast4(command.cardNumber()),
                now
        );
        paymentAttempt.markProcessing(now);

        PaymentAuthorizationResult authorizationResult = paymentProcessorPort.authorize(
                toAuthorizationRequest(command, paymentIntent, paymentAttempt)
        );
        applyAuthorizationResult(checkoutSession, paymentIntent, paymentAttempt, authorizationResult, now);

        paymentAttemptPort.save(paymentAttempt);
        CheckoutSession savedCheckoutSession = checkoutSessionRepositoryPort.save(checkoutSession);
        PaymentIntent savedPaymentIntent = paymentIntentRepositoryPort.save(paymentIntent);

        return new SubmitCheckoutSessionResult(
                savedCheckoutSession.getId(),
                savedPaymentIntent.getId(),
                savedCheckoutSession.getStatus(),
                savedPaymentIntent.getStatus(),
                savedCheckoutSession.getReturnUrl(),
                savedPaymentIntent.getFailureCode(),
                savedPaymentIntent.getFailureMessage(),
                authorizationResult.acsUrl(),
                authorizationResult.threeDsFlow()
        );
    }

    private PaymentAuthorizationRequest toAuthorizationRequest(SubmitCheckoutSessionCommand command,
                                                              PaymentIntent paymentIntent,
                                                              PaymentAttempt paymentAttempt) {
        return new PaymentAuthorizationRequest(
                paymentIntent.getId(),
                paymentAttempt.getId(),
                paymentIntent.getMerchantId(),
                paymentIntent.getAmount(),
                paymentIntent.getCurrency().getCurrencyCode(),
                resolveOrderId(paymentIntent),
                1,
                paymentIntent.getCaptureMethod() == CaptureMethod.AUTOMATIC,
                command.cardNumber(),
                command.expiryMonth(),
                command.expiryYear(),
                command.cvc(),
                command.cardHolderName()
        );
    }

    private void applyAuthorizationResult(CheckoutSession checkoutSession,
                                          PaymentIntent paymentIntent,
                                          PaymentAttempt paymentAttempt,
                                          PaymentAuthorizationResult authorizationResult,
                                          Instant now) {
        switch (authorizationResult.status()) {
            case AUTHORIZED -> {
                applyAuthorizedResult(paymentIntent, paymentAttempt, authorizationResult, now);
                checkoutSession.submit(now);
            }
            case DECLINED -> {
                String failureCode = resolveFailureCode(authorizationResult);
                String failureMessage = resolveFailureMessage(authorizationResult);
                paymentAttempt.markDeclined(
                        authorizationResult.posProvider(),
                        authorizationResult.responseCode(),
                        authorizationResult.responseMessage(),
                        failureCode,
                        failureMessage,
                        now
                );
                paymentIntent.markRequiresPaymentMethod(failureCode, failureMessage, now);
                checkoutSession.submit(now);
            }
            case ERROR -> {
                String failureCode = resolveFailureCode(authorizationResult);
                String failureMessage = resolveFailureMessage(authorizationResult);
                paymentAttempt.markFailed(failureCode, failureMessage, now);
                paymentIntent.markFailed(failureCode, failureMessage, now);
                checkoutSession.submit(now);
            }
            case PENDING -> {
                paymentAttempt.markPending3ds(
                        authorizationResult.threeDsSessionId(),
                        authorizationResult.threeDsFlow(),
                        now
                );
                paymentIntent.markRequiresAction(now);
                checkoutSession.markActionRequired(now);
            }
        }
    }

    private void applyAuthorizedResult(PaymentIntent paymentIntent,
                                       PaymentAttempt paymentAttempt,
                                       PaymentAuthorizationResult authorizationResult,
                                       Instant now) {
        if (paymentIntent.getCaptureMethod() == CaptureMethod.MANUAL) {
            Instant authorizationExpiresAt = now.plus(AUTHORIZATION_WINDOW);
            paymentAttempt.markAuthorized(
                    authorizationResult.posProvider(),
                    authorizationResult.posTransactionId(),
                    authorizationResult.authorizationCode(),
                    authorizationResult.posReference(),
                    authorizationResult.responseCode(),
                    authorizationResult.responseMessage(),
                    authorizationExpiresAt,
                    now
            );
            paymentIntent.markRequiresCapture(paymentAttempt.getId(), authorizationExpiresAt, now);
            return;
        }

        paymentAttempt.markSucceeded(
                authorizationResult.posProvider(),
                authorizationResult.posTransactionId(),
                authorizationResult.authorizationCode(),
                authorizationResult.posReference(),
                authorizationResult.responseCode(),
                authorizationResult.responseMessage(),
                now
        );
        paymentIntent.markSucceeded(now);
    }

    private String resolveOrderId(PaymentIntent paymentIntent) {
        if (paymentIntent.getMerchantReference() == null || paymentIntent.getMerchantReference().isBlank()) {
            return paymentIntent.getId().toString();
        }
        return paymentIntent.getMerchantReference();
    }

    private String resolveCardBrand(String cardNumber) {
        String normalizedCardNumber = normalizeCardNumber(cardNumber);

        if (normalizedCardNumber.startsWith("4")) {
            return "VISA";
        }
        if (normalizedCardNumber.startsWith("5")) {
            return "MASTERCARD";
        }
        if (normalizedCardNumber.startsWith("34") || normalizedCardNumber.startsWith("37")) {
            return "AMEX";
        }
        return "UNKNOWN";
    }

    private String resolveCardLast4(String cardNumber) {
        String normalizedCardNumber = normalizeCardNumber(cardNumber);
        if (normalizedCardNumber.length() < 4) {
            throw new IllegalArgumentException("cardNumber must contain at least 4 digits");
        }
        return normalizedCardNumber.substring(normalizedCardNumber.length() - 4);
    }

    private String normalizeCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return "";
        }
        return cardNumber.replaceAll("\\D", "");
    }

    private String resolveFailureCode(PaymentAuthorizationResult authorizationResult) {
        if (authorizationResult.failureCode() != null && !authorizationResult.failureCode().isBlank()) {
            return authorizationResult.failureCode();
        }
        if (authorizationResult.responseCode() != null && !authorizationResult.responseCode().isBlank()) {
            return authorizationResult.responseCode();
        }
        return "UNKNOWN_POS_ERROR";
    }

    private String resolveFailureMessage(PaymentAuthorizationResult authorizationResult) {
        if (authorizationResult.failureMessage() != null && !authorizationResult.failureMessage().isBlank()) {
            return authorizationResult.failureMessage();
        }
        if (authorizationResult.responseMessage() != null && !authorizationResult.responseMessage().isBlank()) {
            return authorizationResult.responseMessage();
        }
        return "Payment authorization failed";
    }
}
