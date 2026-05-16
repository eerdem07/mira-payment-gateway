package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.PaymentIntentNotFoundException;
import com.eerdem07.mira.gateway.payments.application.exception.RefundCannotBeCreatedException;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateRefundCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateRefundResult;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateRefundUseCase;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAttemptPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentProcessorPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentRefundRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentRefundResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentRefundStatus;
import com.eerdem07.mira.gateway.payments.application.port.out.RefundRepositoryPort;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttempt;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttemptStatus;
import com.eerdem07.mira.gateway.payments.domain.PaymentFailureCode;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntentStatus;
import com.eerdem07.mira.gateway.payments.domain.Refund;
import com.eerdem07.mira.gateway.payments.domain.RefundStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

@Service
public class CreateRefundService implements CreateRefundUseCase {

    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort;
    private final PaymentAttemptPort paymentAttemptPort;
    private final RefundRepositoryPort refundRepositoryPort;
    private final PaymentProcessorPort paymentProcessorPort;
    private final Clock clock;

    public CreateRefundService(PaymentIntentRepositoryPort paymentIntentRepositoryPort,
                               PaymentAttemptPort paymentAttemptPort,
                               RefundRepositoryPort refundRepositoryPort,
                               PaymentProcessorPort paymentProcessorPort,
                               Clock clock) {
        this.paymentIntentRepositoryPort = paymentIntentRepositoryPort;
        this.paymentAttemptPort = paymentAttemptPort;
        this.refundRepositoryPort = refundRepositoryPort;
        this.paymentProcessorPort = paymentProcessorPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CreateRefundResult execute(CreateRefundCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        PaymentIntent paymentIntent = paymentIntentRepositoryPort.findById(command.paymentIntentId())
                .orElseThrow(() -> new PaymentIntentNotFoundException(command.paymentIntentId()));
        ensurePaymentIntentBelongsToMerchant(paymentIntent, command.merchantId());
        ensureRefundCreatableForIntent(paymentIntent);

        PaymentAttempt paymentAttempt = paymentAttemptPort
                .findLatestByPaymentIntentIdAndStatus(paymentIntent.getId(), PaymentAttemptStatus.SUCCEEDED)
                .orElseThrow(() -> new RefundCannotBeCreatedException(
                        "Payment intent does not have a successful payment attempt."
                ));

        Instant now = Instant.now(clock);
        Currency currency = resolveCurrency(command.currency());
        validateRefundRequest(paymentIntent, paymentAttempt, command.amount(), currency);

        Refund refund = Refund.create(
                UUID.randomUUID(),
                paymentIntent.getId(),
                paymentAttempt.getId(),
                command.amount(),
                currency,
                paymentAttempt.getPosProvider(),
                now
        );
        refund.markProcessing(now);

        PaymentRefundResult refundResult = paymentProcessorPort.refund(
                toRefundRequest(paymentIntent, paymentAttempt, refund)
        );
        applyRefundResult(paymentIntent, paymentAttempt, refund, refundResult, now);

        Refund savedRefund = refundRepositoryPort.save(refund);
        paymentAttemptPort.save(paymentAttempt);
        paymentIntentRepositoryPort.save(paymentIntent);

        return new CreateRefundResult(
                savedRefund.getId(),
                savedRefund.getPaymentIntentId(),
                savedRefund.getPaymentAttemptId(),
                savedRefund.getStatus(),
                savedRefund.getAmount(),
                savedRefund.getCurrency().getCurrencyCode(),
                savedRefund.getPosProvider(),
                savedRefund.getPosRefundId(),
                savedRefund.getPosReference(),
                savedRefund.getPosResponseCode(),
                savedRefund.getPosResponseMessage(),
                savedRefund.getFailureCode(),
                savedRefund.getFailureMessage(),
                savedRefund.getCreatedAt(),
                savedRefund.getUpdatedAt(),
                savedRefund.getSucceededAt(),
                savedRefund.getFailedAt()
        );
    }

    private PaymentRefundRequest toRefundRequest(PaymentIntent paymentIntent,
                                                 PaymentAttempt paymentAttempt,
                                                 Refund refund) {
        return new PaymentRefundRequest(
                paymentIntent.getId(),
                refund.getId(),
                paymentAttempt.getId(),
                paymentIntent.getMerchantId(),
                refund.getAmount(),
                refund.getCurrency().getCurrencyCode(),
                resolveOrderId(paymentIntent),
                paymentAttempt.getPosTransactionId(),
                paymentAttempt.getPosAuthCode(),
                paymentAttempt.getPosReference()
        );
    }

    private void applyRefundResult(PaymentIntent paymentIntent,
                                   PaymentAttempt paymentAttempt,
                                   Refund refund,
                                   PaymentRefundResult refundResult,
                                   Instant now) {
        if (refundResult.status() == PaymentRefundStatus.SUCCEEDED) {
            refund.markSucceeded(
                    refundResult.posRefundId(),
                    refundResult.posReference(),
                    refundResult.responseCode(),
                    refundResult.responseMessage(),
                    now
            );
            paymentAttempt.markRefunded(now);
            paymentIntent.markRefunded(now);
            return;
        }

        refund.markFailed(
                resolveFailureCode(refundResult),
                resolveFailureMessage(refundResult),
                refundResult.responseCode(),
                refundResult.responseMessage(),
                now
        );
    }

    private void ensurePaymentIntentBelongsToMerchant(PaymentIntent paymentIntent, UUID merchantId) {
        if (!paymentIntent.getMerchantId().equals(merchantId)) {
            throw new PaymentIntentNotFoundException(paymentIntent.getId());
        }
    }

    private void ensureRefundCreatableForIntent(PaymentIntent paymentIntent) {
        if (paymentIntent.getStatus() != PaymentIntentStatus.SUCCEEDED) {
            throw new RefundCannotBeCreatedException("Payment intent must be SUCCEEDED to create a refund.");
        }

        if (refundRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntent.getId(), RefundStatus.CREATED)
                || refundRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntent.getId(), RefundStatus.PROCESSING)
                || refundRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntent.getId(), RefundStatus.PENDING)
                || refundRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntent.getId(), RefundStatus.SUCCEEDED)) {
            throw new RefundCannotBeCreatedException("Payment intent already has an active or succeeded refund.");
        }
    }

    private void validateRefundRequest(PaymentIntent paymentIntent,
                                       PaymentAttempt paymentAttempt,
                                       BigDecimal amount,
                                       Currency currency) {
        if (!paymentIntent.getId().equals(paymentAttempt.getPaymentIntentId())) {
            throw new RefundCannotBeCreatedException("Payment attempt does not belong to payment intent.");
        }

        if (paymentAttempt.getStatus() != PaymentAttemptStatus.SUCCEEDED) {
            throw new RefundCannotBeCreatedException("Payment attempt must be SUCCEEDED to create a refund.");
        }

        validateAmount(paymentIntent, amount);
        validateCurrency(paymentIntent, currency);
        validatePosProvider(paymentAttempt);
        validateAuthorizationPosMetadata(paymentAttempt);
    }

    private void validateAmount(PaymentIntent paymentIntent, BigDecimal amount) {
        if (amount == null) {
            throw new RefundCannotBeCreatedException("Refund amount must not be null.");
        }

        if (paymentIntent.getAmount().compareTo(amount) != 0) {
            throw new RefundCannotBeCreatedException(
                    "Partial refund is not supported yet. Refund amount must equal payment intent amount."
            );
        }
    }

    private void validateCurrency(PaymentIntent paymentIntent, Currency currency) {
        if (!paymentIntent.getCurrency().equals(currency)) {
            throw new RefundCannotBeCreatedException("Refund currency must equal payment intent currency.");
        }
    }

    private void validatePosProvider(PaymentAttempt paymentAttempt) {
        if (paymentAttempt.getPosProvider() == null || paymentAttempt.getPosProvider().isBlank()) {
            throw new RefundCannotBeCreatedException("Successful payment attempt does not have a POS provider.");
        }
    }

    private void validateAuthorizationPosMetadata(PaymentAttempt paymentAttempt) {
        if (paymentAttempt.getPosTransactionId() == null || paymentAttempt.getPosTransactionId().isBlank()) {
            throw new RefundCannotBeCreatedException("Successful payment attempt does not have a POS transaction id.");
        }
        if (paymentAttempt.getPosAuthCode() == null || paymentAttempt.getPosAuthCode().isBlank()) {
            throw new RefundCannotBeCreatedException("Successful payment attempt does not have a POS authorization code.");
        }
        if (paymentAttempt.getPosReference() == null || paymentAttempt.getPosReference().isBlank()) {
            throw new RefundCannotBeCreatedException("Successful payment attempt does not have a POS reference.");
        }
    }

    private String resolveOrderId(PaymentIntent paymentIntent) {
        if (paymentIntent.getMerchantReference() == null || paymentIntent.getMerchantReference().isBlank()) {
            return paymentIntent.getId().toString();
        }
        return paymentIntent.getMerchantReference();
    }

    private String resolveFailureCode(PaymentRefundResult refundResult) {
        if (refundResult.failureCode() != null && !refundResult.failureCode().isBlank()) {
            return refundResult.failureCode();
        }
        if (refundResult.responseCode() != null && !refundResult.responseCode().isBlank()) {
            return refundResult.responseCode();
        }
        return PaymentFailureCode.UNKNOWN_POS_ERROR.name();
    }

    private String resolveFailureMessage(PaymentRefundResult refundResult) {
        if (refundResult.failureMessage() != null && !refundResult.failureMessage().isBlank()) {
            return refundResult.failureMessage();
        }
        if (refundResult.responseMessage() != null && !refundResult.responseMessage().isBlank()) {
            return refundResult.responseMessage();
        }
        return "Payment refund failed";
    }

    private Currency resolveCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new RefundCannotBeCreatedException("Refund currency must not be blank.");
        }

        return Currency.getInstance(currency);
    }
}
