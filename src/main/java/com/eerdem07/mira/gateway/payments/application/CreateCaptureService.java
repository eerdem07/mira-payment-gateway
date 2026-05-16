package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.CaptureCannotBeCreatedException;
import com.eerdem07.mira.gateway.payments.application.exception.PaymentAttemptNotFoundException;
import com.eerdem07.mira.gateway.payments.application.exception.PaymentIntentNotFoundException;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateCaptureCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateCaptureResult;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateCaptureUseCase;
import com.eerdem07.mira.gateway.payments.application.port.out.CaptureRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentCaptureRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentCaptureResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentCaptureStatus;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAttemptPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentProcessorPort;
import com.eerdem07.mira.gateway.payments.domain.Capture;
import com.eerdem07.mira.gateway.payments.domain.CaptureMethod;
import com.eerdem07.mira.gateway.payments.domain.CaptureStatus;
import com.eerdem07.mira.gateway.payments.domain.PaymentFailureCode;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttempt;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttemptStatus;
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
public class CreateCaptureService implements CreateCaptureUseCase {

    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort;
    private final PaymentAttemptPort paymentAttemptPort;
    private final CaptureRepositoryPort captureRepositoryPort;
    private final PaymentProcessorPort paymentProcessorPort;
    private final Clock clock;

    public CreateCaptureService(PaymentIntentRepositoryPort paymentIntentRepositoryPort,
                                PaymentAttemptPort paymentAttemptPort,
                                CaptureRepositoryPort captureRepositoryPort,
                                PaymentProcessorPort paymentProcessorPort,
                                Clock clock) {
        this.paymentIntentRepositoryPort = paymentIntentRepositoryPort;
        this.paymentAttemptPort = paymentAttemptPort;
        this.captureRepositoryPort = captureRepositoryPort;
        this.paymentProcessorPort = paymentProcessorPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CreateCaptureResult execute(CreateCaptureCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        PaymentIntent paymentIntent = paymentIntentRepositoryPort.findById(command.paymentIntentId())
                .orElseThrow(() -> new PaymentIntentNotFoundException(command.paymentIntentId()));
        ensurePaymentIntentBelongsToMerchant(paymentIntent, command.merchantId());

        UUID authorizedPaymentAttemptId = paymentIntent.getAuthorizedPaymentAttemptId();
        if (authorizedPaymentAttemptId == null) {
            throw new CaptureCannotBeCreatedException("Payment intent does not have an authorized payment attempt.");
        }

        PaymentAttempt paymentAttempt = paymentAttemptPort.findById(authorizedPaymentAttemptId)
                .orElseThrow(() -> new PaymentAttemptNotFoundException(authorizedPaymentAttemptId));

        Instant now = Instant.now(clock);
        Currency currency = resolveCurrency(command.currency());
        validateCaptureRequest(paymentIntent, paymentAttempt, command.amount(), currency, now);

        Capture capture = Capture.create(
                UUID.randomUUID(),
                paymentIntent.getId(),
                paymentAttempt.getId(),
                command.amount(),
                currency,
                paymentAttempt.getPosProvider(),
                now
        );
        capture.markProcessing(now);

        PaymentCaptureResult captureResult = paymentProcessorPort.capture(
                toCaptureRequest(paymentIntent, paymentAttempt, capture)
        );
        applyCaptureResult(paymentIntent, paymentAttempt, capture, captureResult, now);

        Capture savedCapture = captureRepositoryPort.save(capture);
        paymentAttemptPort.save(paymentAttempt);
        paymentIntentRepositoryPort.save(paymentIntent);

        return new CreateCaptureResult(
                savedCapture.getId(),
                savedCapture.getPaymentIntentId(),
                savedCapture.getPaymentAttemptId(),
                savedCapture.getStatus(),
                savedCapture.getAmount(),
                savedCapture.getCurrency().getCurrencyCode(),
                savedCapture.getPosProvider(),
                savedCapture.getPosCaptureId(),
                savedCapture.getPosReference(),
                savedCapture.getPosResponseCode(),
                savedCapture.getPosResponseMessage(),
                savedCapture.getFailureCode(),
                savedCapture.getFailureMessage(),
                savedCapture.getCreatedAt(),
                savedCapture.getUpdatedAt(),
                savedCapture.getSucceededAt(),
                savedCapture.getFailedAt()
        );
    }

    private PaymentCaptureRequest toCaptureRequest(PaymentIntent paymentIntent,
                                                   PaymentAttempt paymentAttempt,
                                                   Capture capture) {
        return new PaymentCaptureRequest(
                paymentIntent.getId(),
                capture.getId(),
                paymentAttempt.getId(),
                paymentIntent.getMerchantId(),
                capture.getAmount(),
                capture.getCurrency().getCurrencyCode(),
                resolveOrderId(paymentIntent),
                paymentAttempt.getPosTransactionId(),
                paymentAttempt.getPosAuthCode(),
                paymentAttempt.getPosReference()
        );
    }

    private void applyCaptureResult(PaymentIntent paymentIntent,
                                    PaymentAttempt paymentAttempt,
                                    Capture capture,
                                    PaymentCaptureResult captureResult,
                                    Instant now) {
        if (captureResult.status() == PaymentCaptureStatus.SUCCEEDED) {
            capture.markSucceeded(
                    captureResult.posCaptureId(),
                    captureResult.posReference(),
                    captureResult.responseCode(),
                    captureResult.responseMessage(),
                    now
            );
            paymentAttempt.markCaptured(now);
            paymentIntent.markSucceeded(now);
            return;
        }

        capture.markFailed(
                resolveFailureCode(captureResult),
                resolveFailureMessage(captureResult),
                captureResult.responseCode(),
                captureResult.responseMessage(),
                now
        );
    }

    private void ensurePaymentIntentBelongsToMerchant(PaymentIntent paymentIntent, UUID merchantId) {
        if (!paymentIntent.getMerchantId().equals(merchantId)) {
            throw new PaymentIntentNotFoundException(paymentIntent.getId());
        }
    }

    private void validateCaptureRequest(PaymentIntent paymentIntent,
                                        PaymentAttempt paymentAttempt,
                                        BigDecimal amount,
                                        Currency currency,
                                        Instant now) {
        if (paymentIntent.getStatus() != PaymentIntentStatus.REQUIRES_CAPTURE) {
            throw new CaptureCannotBeCreatedException(
                    "Payment intent must be REQUIRES_CAPTURE to create a capture."
            );
        }

        if (paymentIntent.getCaptureMethod() != CaptureMethod.MANUAL) {
            throw new CaptureCannotBeCreatedException("Only MANUAL payment intents can be captured separately.");
        }

        if (!paymentIntent.getId().equals(paymentAttempt.getPaymentIntentId())) {
            throw new CaptureCannotBeCreatedException("Payment attempt does not belong to payment intent.");
        }

        if (!paymentAttempt.getId().equals(paymentIntent.getAuthorizedPaymentAttemptId())) {
            throw new CaptureCannotBeCreatedException("Only latest authorized payment attempt can be captured.");
        }

        if (paymentAttempt.getStatus() != PaymentAttemptStatus.AUTHORIZED) {
            throw new CaptureCannotBeCreatedException("Payment attempt must be AUTHORIZED to create a capture.");
        }

        if (paymentAttempt.getAuthorizationExpiresAt() == null || !paymentAttempt.getAuthorizationExpiresAt().isAfter(now)) {
            throw new CaptureCannotBeCreatedException("Payment attempt authorization has expired.");
        }

        if (captureRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntent.getId(), CaptureStatus.CREATED)
                || captureRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntent.getId(), CaptureStatus.PROCESSING)) {
            throw new CaptureCannotBeCreatedException("Payment intent already has an active capture.");
        }

        validateAmount(paymentIntent, amount);
        validateCurrency(paymentIntent, currency);
        validatePosProvider(paymentAttempt);
        validateAuthorizationPosMetadata(paymentAttempt);
    }

    private void validateAmount(PaymentIntent paymentIntent, BigDecimal amount) {
        if (amount == null) {
            throw new CaptureCannotBeCreatedException("Capture amount must not be null.");
        }

        if (paymentIntent.getAmount().compareTo(amount) != 0) {
            throw new CaptureCannotBeCreatedException(
                    "Partial capture is not supported yet. Capture amount must equal payment intent amount."
            );
        }
    }

    private void validateCurrency(PaymentIntent paymentIntent, Currency currency) {
        if (!paymentIntent.getCurrency().equals(currency)) {
            throw new CaptureCannotBeCreatedException("Capture currency must equal payment intent currency.");
        }
    }

    private void validatePosProvider(PaymentAttempt paymentAttempt) {
        if (paymentAttempt.getPosProvider() == null || paymentAttempt.getPosProvider().isBlank()) {
            throw new CaptureCannotBeCreatedException("Authorized payment attempt does not have a POS provider.");
        }
    }

    private void validateAuthorizationPosMetadata(PaymentAttempt paymentAttempt) {
        if (paymentAttempt.getPosTransactionId() == null || paymentAttempt.getPosTransactionId().isBlank()) {
            throw new CaptureCannotBeCreatedException("Authorized payment attempt does not have a POS transaction id.");
        }
        if (paymentAttempt.getPosAuthCode() == null || paymentAttempt.getPosAuthCode().isBlank()) {
            throw new CaptureCannotBeCreatedException("Authorized payment attempt does not have a POS authorization code.");
        }
        if (paymentAttempt.getPosReference() == null || paymentAttempt.getPosReference().isBlank()) {
            throw new CaptureCannotBeCreatedException("Authorized payment attempt does not have a POS reference.");
        }
    }

    private String resolveOrderId(PaymentIntent paymentIntent) {
        if (paymentIntent.getMerchantReference() == null || paymentIntent.getMerchantReference().isBlank()) {
            return paymentIntent.getId().toString();
        }
        return paymentIntent.getMerchantReference();
    }

    private String resolveFailureCode(PaymentCaptureResult captureResult) {
        if (captureResult.failureCode() != null && !captureResult.failureCode().isBlank()) {
            return captureResult.failureCode();
        }
        if (captureResult.responseCode() != null && !captureResult.responseCode().isBlank()) {
            return captureResult.responseCode();
        }
        return PaymentFailureCode.UNKNOWN_POS_ERROR.name();
    }

    private String resolveFailureMessage(PaymentCaptureResult captureResult) {
        if (captureResult.failureMessage() != null && !captureResult.failureMessage().isBlank()) {
            return captureResult.failureMessage();
        }
        if (captureResult.responseMessage() != null && !captureResult.responseMessage().isBlank()) {
            return captureResult.responseMessage();
        }
        return "Payment capture failed";
    }

    private Currency resolveCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new CaptureCannotBeCreatedException("Capture currency must not be blank.");
        }

        return Currency.getInstance(currency);
    }
}
