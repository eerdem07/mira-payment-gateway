package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.CaptureCannotBeCreatedException;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateCaptureCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateCaptureResult;
import com.eerdem07.mira.gateway.payments.application.port.out.CaptureRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAttemptPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentCaptureRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentCaptureResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentCaptureStatus;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentProcessorPort;
import com.eerdem07.mira.gateway.payments.domain.Capture;
import com.eerdem07.mira.gateway.payments.domain.CaptureMethod;
import com.eerdem07.mira.gateway.payments.domain.CaptureStatus;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttempt;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttemptStatus;
import com.eerdem07.mira.gateway.payments.domain.PaymentFailureCode;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntentStatus;
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

class CreateCaptureServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-13T08:30:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort = mock(PaymentIntentRepositoryPort.class);
    private final PaymentAttemptPort paymentAttemptPort = mock(PaymentAttemptPort.class);
    private final CaptureRepositoryPort captureRepositoryPort = mock(CaptureRepositoryPort.class);
    private final PaymentProcessorPort paymentProcessorPort = mock(PaymentProcessorPort.class);

    private final CreateCaptureService service = new CreateCaptureService(
            paymentIntentRepositoryPort,
            paymentAttemptPort,
            captureRepositoryPort,
            paymentProcessorPort,
            CLOCK
    );

    @Test
    void shouldCaptureAuthorizedManualPaymentIntent() {
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();
        PaymentIntent paymentIntent = manualIntent(merchantId, paymentIntentId, paymentAttemptId);
        PaymentAttempt paymentAttempt = authorizedAttempt(paymentIntentId, paymentAttemptId);

        when(paymentIntentRepositoryPort.findById(paymentIntentId)).thenReturn(Optional.of(paymentIntent));
        when(paymentAttemptPort.findById(paymentAttemptId)).thenReturn(Optional.of(paymentAttempt));
        when(captureRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntentId, CaptureStatus.CREATED)).thenReturn(false);
        when(captureRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntentId, CaptureStatus.PROCESSING)).thenReturn(false);
        when(paymentProcessorPort.capture(any(PaymentCaptureRequest.class))).thenReturn(new PaymentCaptureResult(
                PaymentCaptureStatus.SUCCEEDED,
                "MOCK_BANK_POS",
                "pos_cap_123",
                "HST-CAP-123",
                "00",
                "Capture approved",
                null,
                null
        ));
        when(captureRepositoryPort.save(any(Capture.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentAttemptPort.save(any(PaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentIntentRepositoryPort.save(any(PaymentIntent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCaptureResult result = service.execute(new CreateCaptureCommand(
                merchantId,
                paymentIntentId,
                new BigDecimal("1250.50"),
                "TRY"
        ));

        assertThat(result.status()).isEqualTo(CaptureStatus.SUCCEEDED);
        assertThat(result.posCaptureId()).isEqualTo("pos_cap_123");
        assertThat(result.posReference()).isEqualTo("HST-CAP-123");
        assertThat(result.posResponseCode()).isEqualTo("00");
        assertThat(result.succeededAt()).isEqualTo(NOW);
        assertThat(paymentIntent.getStatus()).isEqualTo(PaymentIntentStatus.SUCCEEDED);
        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.SUCCEEDED);

        ArgumentCaptor<PaymentCaptureRequest> requestCaptor = ArgumentCaptor.forClass(PaymentCaptureRequest.class);
        verify(paymentProcessorPort).capture(requestCaptor.capture());
        PaymentCaptureRequest request = requestCaptor.getValue();
        assertThat(request.captureId()).isEqualTo(result.id());
        assertThat(request.originalPaymentAttemptId()).isEqualTo(paymentAttemptId);
        assertThat(request.orderId()).isEqualTo("order-123");
        assertThat(request.originalPosTransactionId()).isEqualTo("pos_txn_123");
        assertThat(request.authorizationCode()).isEqualTo("AUTH123");
        assertThat(request.posReference()).isEqualTo("HST-AUTH-123");
    }

    @Test
    void shouldKeepPaymentIntentRequiresCaptureWhenPosCaptureFails() {
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();
        PaymentIntent paymentIntent = manualIntent(merchantId, paymentIntentId, paymentAttemptId);
        PaymentAttempt paymentAttempt = authorizedAttempt(paymentIntentId, paymentAttemptId);

        when(paymentIntentRepositoryPort.findById(paymentIntentId)).thenReturn(Optional.of(paymentIntent));
        when(paymentAttemptPort.findById(paymentAttemptId)).thenReturn(Optional.of(paymentAttempt));
        when(captureRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntentId, CaptureStatus.CREATED)).thenReturn(false);
        when(captureRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntentId, CaptureStatus.PROCESSING)).thenReturn(false);
        when(paymentProcessorPort.capture(any(PaymentCaptureRequest.class))).thenReturn(new PaymentCaptureResult(
                PaymentCaptureStatus.FAILED,
                "MOCK_BANK_POS",
                null,
                null,
                "12",
                "Invalid transaction",
                PaymentFailureCode.UNKNOWN_POS_ERROR.name(),
                "Invalid transaction"
        ));
        when(captureRepositoryPort.save(any(Capture.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCaptureResult result = service.execute(new CreateCaptureCommand(
                merchantId,
                paymentIntentId,
                new BigDecimal("1250.50"),
                "TRY"
        ));

        assertThat(result.status()).isEqualTo(CaptureStatus.FAILED);
        assertThat(result.failureCode()).isEqualTo(PaymentFailureCode.UNKNOWN_POS_ERROR.name());
        assertThat(result.failureMessage()).isEqualTo("Invalid transaction");
        assertThat(result.posResponseCode()).isEqualTo("12");
        assertThat(result.failedAt()).isEqualTo(NOW);
        assertThat(paymentIntent.getStatus()).isEqualTo(PaymentIntentStatus.REQUIRES_CAPTURE);
        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.AUTHORIZED);
    }

    @Test
    void shouldRejectPartialCaptureBeforeCallingPos() {
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();

        when(paymentIntentRepositoryPort.findById(paymentIntentId))
                .thenReturn(Optional.of(manualIntent(merchantId, paymentIntentId, paymentAttemptId)));
        when(paymentAttemptPort.findById(paymentAttemptId))
                .thenReturn(Optional.of(authorizedAttempt(paymentIntentId, paymentAttemptId)));

        assertThatThrownBy(() -> service.execute(new CreateCaptureCommand(
                merchantId,
                paymentIntentId,
                new BigDecimal("1000.00"),
                "TRY"
        ))).isInstanceOf(CaptureCannotBeCreatedException.class)
                .hasMessage("Partial capture is not supported yet. Capture amount must equal payment intent amount.");

        verify(paymentProcessorPort, never()).capture(any(PaymentCaptureRequest.class));
        verify(captureRepositoryPort, never()).save(any(Capture.class));
    }

    private PaymentIntent manualIntent(UUID merchantId, UUID paymentIntentId, UUID paymentAttemptId) {
        return PaymentIntent.restore(
                paymentIntentId,
                merchantId,
                new BigDecimal("1250.50"),
                Currency.getInstance("TRY"),
                CaptureMethod.MANUAL,
                "order-123",
                "manual capture payment",
                PaymentIntentStatus.REQUIRES_CAPTURE,
                0,
                null,
                null,
                NOW.plusSeconds(3600),
                NOW.plusSeconds(3600),
                paymentAttemptId,
                NOW.minusSeconds(60),
                NOW.minusSeconds(30),
                null,
                null
        );
    }

    private PaymentAttempt authorizedAttempt(UUID paymentIntentId, UUID paymentAttemptId) {
        return PaymentAttempt.restore(
                paymentAttemptId,
                paymentIntentId,
                UUID.randomUUID(),
                PaymentAttemptStatus.AUTHORIZED,
                new BigDecimal("1250.50"),
                Currency.getInstance("TRY"),
                "VISA",
                "4242",
                "MOCK_BANK_POS",
                "pos_txn_123",
                "AUTH123",
                "HST-AUTH-123",
                "00",
                "Authorized",
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
                NOW.minusSeconds(30),
                NOW.plusSeconds(3600),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
