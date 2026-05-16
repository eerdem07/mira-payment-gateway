package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.RefundCannotBeCreatedException;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateRefundCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateRefundResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAttemptPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentProcessorPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentRefundRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentRefundResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentRefundStatus;
import com.eerdem07.mira.gateway.payments.application.port.out.RefundRepositoryPort;
import com.eerdem07.mira.gateway.payments.domain.CaptureMethod;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttempt;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttemptStatus;
import com.eerdem07.mira.gateway.payments.domain.PaymentFailureCode;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntentStatus;
import com.eerdem07.mira.gateway.payments.domain.Refund;
import com.eerdem07.mira.gateway.payments.domain.RefundStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateRefundServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-13T09:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort = mock(PaymentIntentRepositoryPort.class);
    private final PaymentAttemptPort paymentAttemptPort = mock(PaymentAttemptPort.class);
    private final RefundRepositoryPort refundRepositoryPort = mock(RefundRepositoryPort.class);
    private final PaymentProcessorPort paymentProcessorPort = mock(PaymentProcessorPort.class);

    private final CreateRefundService service = new CreateRefundService(
            paymentIntentRepositoryPort,
            paymentAttemptPort,
            refundRepositoryPort,
            paymentProcessorPort,
            CLOCK
    );

    @Test
    void shouldRefundSucceededPaymentIntent() {
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();
        PaymentIntent paymentIntent = succeededIntent(merchantId, paymentIntentId);
        PaymentAttempt paymentAttempt = succeededAttempt(paymentIntentId, paymentAttemptId);

        when(paymentIntentRepositoryPort.findById(paymentIntentId)).thenReturn(Optional.of(paymentIntent));
        when(paymentAttemptPort.findLatestByPaymentIntentIdAndStatus(paymentIntentId, PaymentAttemptStatus.SUCCEEDED))
                .thenReturn(Optional.of(paymentAttempt));
        when(paymentProcessorPort.refund(any(PaymentRefundRequest.class))).thenReturn(new PaymentRefundResult(
                PaymentRefundStatus.SUCCEEDED,
                "MOCK_BANK_POS",
                "pos_ref_123",
                "HST-REF-123",
                "00",
                "Refund approved",
                null,
                null
        ));
        when(refundRepositoryPort.save(any(Refund.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateRefundResult result = service.execute(new CreateRefundCommand(
                merchantId,
                paymentIntentId,
                new BigDecimal("1250.50"),
                "TRY"
        ));

        assertThat(result.status()).isEqualTo(RefundStatus.SUCCEEDED);
        assertThat(result.posRefundId()).isEqualTo("pos_ref_123");
        assertThat(result.posReference()).isEqualTo("HST-REF-123");
        assertThat(result.posResponseCode()).isEqualTo("00");
        assertThat(result.succeededAt()).isEqualTo(NOW);
        assertThat(paymentIntent.getStatus()).isEqualTo(PaymentIntentStatus.REFUNDED);
        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.REFUNDED);

        ArgumentCaptor<PaymentRefundRequest> requestCaptor = ArgumentCaptor.forClass(PaymentRefundRequest.class);
        verify(paymentProcessorPort).refund(requestCaptor.capture());
        PaymentRefundRequest request = requestCaptor.getValue();
        assertThat(request.refundId()).isEqualTo(result.id());
        assertThat(request.originalPaymentAttemptId()).isEqualTo(paymentAttemptId);
        assertThat(request.orderId()).isEqualTo("order-123");
        assertThat(request.originalPosTransactionId()).isEqualTo("pos_txn_123");
        assertThat(request.authorizationCode()).isEqualTo("AUTH123");
        assertThat(request.posReference()).isEqualTo("HST-AUTH-123");
    }

    @Test
    void shouldKeepPaymentSucceededWhenPosRefundFails() {
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();
        PaymentIntent paymentIntent = succeededIntent(merchantId, paymentIntentId);
        PaymentAttempt paymentAttempt = succeededAttempt(paymentIntentId, paymentAttemptId);

        when(paymentIntentRepositoryPort.findById(paymentIntentId)).thenReturn(Optional.of(paymentIntent));
        when(paymentAttemptPort.findLatestByPaymentIntentIdAndStatus(paymentIntentId, PaymentAttemptStatus.SUCCEEDED))
                .thenReturn(Optional.of(paymentAttempt));
        when(paymentProcessorPort.refund(any(PaymentRefundRequest.class))).thenReturn(new PaymentRefundResult(
                PaymentRefundStatus.FAILED,
                "MOCK_BANK_POS",
                null,
                null,
                "12",
                "Invalid transaction",
                PaymentFailureCode.UNKNOWN_POS_ERROR.name(),
                "Invalid transaction"
        ));
        when(refundRepositoryPort.save(any(Refund.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateRefundResult result = service.execute(new CreateRefundCommand(
                merchantId,
                paymentIntentId,
                new BigDecimal("1250.50"),
                "TRY"
        ));

        assertThat(result.status()).isEqualTo(RefundStatus.FAILED);
        assertThat(result.failureCode()).isEqualTo(PaymentFailureCode.UNKNOWN_POS_ERROR.name());
        assertThat(result.failureMessage()).isEqualTo("Invalid transaction");
        assertThat(result.posResponseCode()).isEqualTo("12");
        assertThat(result.failedAt()).isEqualTo(NOW);
        assertThat(paymentIntent.getStatus()).isEqualTo(PaymentIntentStatus.SUCCEEDED);
        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.SUCCEEDED);
    }

    @Test
    void shouldRejectPartialRefundBeforeCallingPos() {
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();

        when(paymentIntentRepositoryPort.findById(paymentIntentId))
                .thenReturn(Optional.of(succeededIntent(merchantId, paymentIntentId)));
        when(paymentAttemptPort.findLatestByPaymentIntentIdAndStatus(paymentIntentId, PaymentAttemptStatus.SUCCEEDED))
                .thenReturn(Optional.of(succeededAttempt(paymentIntentId, paymentAttemptId)));

        assertThatThrownBy(() -> service.execute(new CreateRefundCommand(
                merchantId,
                paymentIntentId,
                new BigDecimal("1000.00"),
                "TRY"
        ))).isInstanceOf(RefundCannotBeCreatedException.class)
                .hasMessage("Partial refund is not supported yet. Refund amount must equal payment intent amount.");

        verify(paymentProcessorPort, never()).refund(any(PaymentRefundRequest.class));
        verify(refundRepositoryPort, never()).save(any(Refund.class));
    }

    @Test
    void shouldRejectRefundWhenPaymentIntentHasActiveRefund() {
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();

        when(paymentIntentRepositoryPort.findById(paymentIntentId))
                .thenReturn(Optional.of(succeededIntent(merchantId, paymentIntentId)));
        when(refundRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntentId, RefundStatus.PROCESSING))
                .thenReturn(true);

        assertThatThrownBy(() -> service.execute(new CreateRefundCommand(
                merchantId,
                paymentIntentId,
                new BigDecimal("1250.50"),
                "TRY"
        ))).isInstanceOf(RefundCannotBeCreatedException.class)
                .hasMessage("Payment intent already has an active or succeeded refund.");

        verify(paymentProcessorPort, never()).refund(any(PaymentRefundRequest.class));
        verify(refundRepositoryPort, never()).save(any(Refund.class));
    }

    @Test
    void shouldRejectRefundWhenPaymentIntentIsNotSucceeded() {
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();

        when(paymentIntentRepositoryPort.findById(paymentIntentId)).thenReturn(Optional.of(PaymentIntent.restore(
                paymentIntentId,
                merchantId,
                new BigDecimal("1250.50"),
                Currency.getInstance("TRY"),
                CaptureMethod.MANUAL,
                "order-123",
                "manual payment",
                PaymentIntentStatus.REQUIRES_CAPTURE,
                0,
                null,
                null,
                NOW.plusSeconds(3600),
                NOW.plusSeconds(3600),
                UUID.randomUUID(),
                NOW.minusSeconds(60),
                NOW.minusSeconds(30),
                null,
                null
        )));

        assertThatThrownBy(() -> service.execute(new CreateRefundCommand(
                merchantId,
                paymentIntentId,
                new BigDecimal("1250.50"),
                "TRY"
        ))).isInstanceOf(RefundCannotBeCreatedException.class)
                .hasMessage("Payment intent must be SUCCEEDED to create a refund.");

        verify(paymentProcessorPort, never()).refund(any(PaymentRefundRequest.class));
        verify(refundRepositoryPort, never()).save(any(Refund.class));
    }

    private PaymentIntent succeededIntent(UUID merchantId, UUID paymentIntentId) {
        return PaymentIntent.restore(
                paymentIntentId,
                merchantId,
                new BigDecimal("1250.50"),
                Currency.getInstance("TRY"),
                CaptureMethod.AUTOMATIC,
                "order-123",
                "succeeded payment",
                PaymentIntentStatus.SUCCEEDED,
                0,
                null,
                null,
                NOW.plusSeconds(3600),
                null,
                null,
                NOW.minusSeconds(60),
                NOW.minusSeconds(30),
                NOW.minusSeconds(20),
                null
        );
    }

    private PaymentAttempt succeededAttempt(UUID paymentIntentId, UUID paymentAttemptId) {
        return PaymentAttempt.restore(
                paymentAttemptId,
                paymentIntentId,
                UUID.randomUUID(),
                PaymentAttemptStatus.SUCCEEDED,
                new BigDecimal("1250.50"),
                Currency.getInstance("TRY"),
                "VISA",
                "4242",
                "MOCK_BANK_POS",
                "pos_txn_123",
                "AUTH123",
                "HST-AUTH-123",
                "00",
                "Approved",
                null,
                null,
                null,
                null,
                null,
                null,
                NOW.minusSeconds(60),
                NOW.minusSeconds(30),
                NOW.minusSeconds(50),
                null,
                null,
                null,
                NOW.minusSeconds(20),
                null,
                null,
                null,
                null,
                null
        );
    }
}
