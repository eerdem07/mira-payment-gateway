package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.CheckoutSessionNotFoundException;
import com.eerdem07.mira.gateway.payments.application.exception.PaymentAttemptNotFoundException;
import com.eerdem07.mira.gateway.payments.application.exception.PaymentIntentNotFoundException;
import com.eerdem07.mira.gateway.payments.application.port.in.Complete3DsCheckoutSessionCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.Complete3DsCheckoutSessionResult;
import com.eerdem07.mira.gateway.payments.application.port.in.Complete3DsCheckoutSessionUseCase;
import com.eerdem07.mira.gateway.payments.application.port.out.CheckoutSessionRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAttemptPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAuthorizationResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentProcessorPort;
import com.eerdem07.mira.gateway.payments.application.port.out.ThreeDsCompleteRequest;
import com.eerdem07.mira.gateway.payments.domain.CaptureMethod;
import com.eerdem07.mira.gateway.payments.domain.CheckoutSession;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttempt;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttemptStatus;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class Complete3DsCheckoutSessionService implements Complete3DsCheckoutSessionUseCase {

    private static final Duration AUTHORIZATION_WINDOW = Duration.ofDays(7);

    private final CheckoutSessionRepositoryPort checkoutSessionRepositoryPort;
    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort;
    private final PaymentAttemptPort paymentAttemptPort;
    private final PaymentProcessorPort paymentProcessorPort;
    private final Clock clock;

    public Complete3DsCheckoutSessionService(CheckoutSessionRepositoryPort checkoutSessionRepositoryPort,
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
    public Complete3DsCheckoutSessionResult execute(Complete3DsCheckoutSessionCommand command) {
        CheckoutSession checkoutSession = checkoutSessionRepositoryPort.findByToken(command.token())
                .orElseThrow(() -> new CheckoutSessionNotFoundException(command.token()));

        PaymentIntent paymentIntent = paymentIntentRepositoryPort.findById(checkoutSession.getPaymentIntentId())
                .orElseThrow(() -> new PaymentIntentNotFoundException(checkoutSession.getPaymentIntentId()));

        PaymentAttempt paymentAttempt = paymentAttemptPort
                .findLatestByPaymentIntentIdAndStatus(paymentIntent.getId(), PaymentAttemptStatus.REQUIRES_ACTION)
                .orElseThrow(() -> new PaymentAttemptNotFoundException(paymentIntent.getId()));

        Instant now = Instant.now(clock);

        ThreeDsCompleteRequest completeRequest = new ThreeDsCompleteRequest(
                paymentIntent.getId(),
                paymentAttempt.getId(),
                resolveOrderId(paymentIntent),
                paymentAttempt.getThreeDsSessionId()
        );

        PaymentAuthorizationResult result = paymentProcessorPort.complete3ds(completeRequest);

        applyAuthorizationResult(checkoutSession, paymentIntent, paymentAttempt, result, now);

        paymentAttemptPort.save(paymentAttempt);
        CheckoutSession savedCheckoutSession = checkoutSessionRepositoryPort.save(checkoutSession);
        PaymentIntent savedPaymentIntent = paymentIntentRepositoryPort.save(paymentIntent);

        return new Complete3DsCheckoutSessionResult(
                savedCheckoutSession.getId(),
                savedPaymentIntent.getId(),
                savedCheckoutSession.getStatus(),
                savedPaymentIntent.getStatus(),
                savedCheckoutSession.getReturnUrl(),
                savedPaymentIntent.getFailureCode(),
                savedPaymentIntent.getFailureMessage()
        );
    }

    private void applyAuthorizationResult(CheckoutSession checkoutSession,
                                          PaymentIntent paymentIntent,
                                          PaymentAttempt paymentAttempt,
                                          PaymentAuthorizationResult result,
                                          Instant now) {
        switch (result.status()) {
            case AUTHORIZED -> {
                applyAuthorizedResult(paymentIntent, paymentAttempt, result, now);
                checkoutSession.submit(now);
            }
            case DECLINED -> {
                String failureCode = resolveFailureCode(result);
                String failureMessage = resolveFailureMessage(result);
                paymentAttempt.markDeclined(
                        result.posProvider(),
                        result.responseCode(),
                        result.responseMessage(),
                        failureCode,
                        failureMessage,
                        now
                );
                paymentIntent.markRequiresPaymentMethod(failureCode, failureMessage, now);
                checkoutSession.submit(now);
            }
            default -> {
                String failureCode = resolveFailureCode(result);
                String failureMessage = resolveFailureMessage(result);
                paymentAttempt.markFailed(failureCode, failureMessage, now);
                paymentIntent.markFailed(failureCode, failureMessage, now);
                checkoutSession.submit(now);
            }
        }
    }

    private void applyAuthorizedResult(PaymentIntent paymentIntent,
                                       PaymentAttempt paymentAttempt,
                                       PaymentAuthorizationResult result,
                                       Instant now) {
        if (paymentIntent.getCaptureMethod() == CaptureMethod.MANUAL) {
            Instant authorizationExpiresAt = now.plus(AUTHORIZATION_WINDOW);
            paymentAttempt.markAuthorized(
                    result.posProvider(),
                    result.posTransactionId(),
                    result.authorizationCode(),
                    result.posReference(),
                    result.responseCode(),
                    result.responseMessage(),
                    authorizationExpiresAt,
                    now
            );
            paymentIntent.markRequiresCapture(paymentAttempt.getId(), authorizationExpiresAt, now);
            return;
        }

        paymentAttempt.markSucceeded(
                result.posProvider(),
                result.posTransactionId(),
                result.authorizationCode(),
                result.posReference(),
                result.responseCode(),
                result.responseMessage(),
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

    private String resolveFailureCode(PaymentAuthorizationResult result) {
        if (result.failureCode() != null && !result.failureCode().isBlank()) {
            return result.failureCode();
        }
        if (result.responseCode() != null && !result.responseCode().isBlank()) {
            return result.responseCode();
        }
        return "UNKNOWN_POS_ERROR";
    }

    private String resolveFailureMessage(PaymentAuthorizationResult result) {
        if (result.failureMessage() != null && !result.failureMessage().isBlank()) {
            return result.failureMessage();
        }
        if (result.responseMessage() != null && !result.responseMessage().isBlank()) {
            return result.responseMessage();
        }
        return "3DS authentication or authorization failed";
    }
}
