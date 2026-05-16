package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.AuthorizationVoidCannotBeCreatedException;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateAuthorizationVoidCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateAuthorizationVoidResult;
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

class CreateAuthorizationVoidServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-13T08:45:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort = mock(PaymentIntentRepositoryPort.class);
    private final PaymentAttemptPort paymentAttemptPort = mock(PaymentAttemptPort.class);
    private final AuthorizationVoidRepositoryPort authorizationVoidRepositoryPort = mock(AuthorizationVoidRepositoryPort.class);
    private final CaptureRepositoryPort captureRepositoryPort = mock(CaptureRepositoryPort.class);
    private final PaymentProcessorPort paymentProcessorPort = mock(PaymentProcessorPort.class);

    private final CreateAuthorizationVoidService service = new CreateAuthorizationVoidService(
            paymentIntentRepositoryPort,
            paymentAttemptPort,
            authorizationVoidRepositoryPort,
            captureRepositoryPort,
            paymentProcessorPort,
            CLOCK
    );

    @Test
    void shouldVoidAuthorizedManualPaymentIntent() {
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();
        PaymentIntent paymentIntent = manualIntent(merchantId, paymentIntentId, paymentAttemptId);
        PaymentAttempt paymentAttempt = authorizedAttempt(paymentIntentId, paymentAttemptId);

        when(paymentIntentRepositoryPort.findById(paymentIntentId)).thenReturn(Optional.of(paymentIntent));
        when(paymentAttemptPort.findById(paymentAttemptId)).thenReturn(Optional.of(paymentAttempt));
        when(authorizationVoidRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntentId, AuthorizationVoidStatus.CREATED)).thenReturn(false);
        when(authorizationVoidRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntentId, AuthorizationVoidStatus.PROCESSING)).thenReturn(false);
        when(captureRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntentId, CaptureStatus.CREATED)).thenReturn(false);
        when(captureRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntentId, CaptureStatus.PROCESSING)).thenReturn(false);
        when(captureRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntentId, CaptureStatus.SUCCEEDED)).thenReturn(false);
        when(paymentProcessorPort.voidAuthorization(any(PaymentVoidRequest.class))).thenReturn(new PaymentVoidResult(
                PaymentVoidStatus.SUCCEEDED,
                "MOCK_BANK_POS",
                "pos_void_123",
                "HST-VOID-123",
                "00",
                "Void approved",
                null,
                null
        ));
        when(authorizationVoidRepositoryPort.save(any(AuthorizationVoid.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentAttemptPort.save(any(PaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentIntentRepositoryPort.save(any(PaymentIntent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateAuthorizationVoidResult result = service.execute(new CreateAuthorizationVoidCommand(
                merchantId,
                paymentIntentId,
                new BigDecimal("1250.50"),
                "TRY"
        ));

        assertThat(result.status()).isEqualTo(AuthorizationVoidStatus.SUCCEEDED);
        assertThat(result.posVoidId()).isEqualTo("pos_void_123");
        assertThat(result.posReference()).isEqualTo("HST-VOID-123");
        assertThat(result.posResponseCode()).isEqualTo("00");
        assertThat(result.succeededAt()).isEqualTo(NOW);
        assertThat(paymentIntent.getStatus()).isEqualTo(PaymentIntentStatus.CANCELED);
        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.VOIDED);

        ArgumentCaptor<PaymentVoidRequest> requestCaptor = ArgumentCaptor.forClass(PaymentVoidRequest.class);
        verify(paymentProcessorPort).voidAuthorization(requestCaptor.capture());
        PaymentVoidRequest request = requestCaptor.getValue();
        assertThat(request.voidId()).isEqualTo(result.id());
        assertThat(request.originalPaymentAttemptId()).isEqualTo(paymentAttemptId);
        assertThat(request.orderId()).isEqualTo("order-123");
        assertThat(request.originalPosTransactionId()).isEqualTo("pos_txn_123");
        assertThat(request.authorizationCode()).isEqualTo("AUTH123");
        assertThat(request.posReference()).isEqualTo("HST-AUTH-123");
    }

    @Test
    void shouldKeepAuthorizationOpenWhenPosVoidFails() {
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();
        PaymentIntent paymentIntent = manualIntent(merchantId, paymentIntentId, paymentAttemptId);
        PaymentAttempt paymentAttempt = authorizedAttempt(paymentIntentId, paymentAttemptId);

        when(paymentIntentRepositoryPort.findById(paymentIntentId)).thenReturn(Optional.of(paymentIntent));
        when(paymentAttemptPort.findById(paymentAttemptId)).thenReturn(Optional.of(paymentAttempt));
        when(paymentProcessorPort.voidAuthorization(any(PaymentVoidRequest.class))).thenReturn(new PaymentVoidResult(
                PaymentVoidStatus.FAILED,
                "MOCK_BANK_POS",
                null,
                null,
                "12",
                "Invalid transaction",
                PaymentFailureCode.UNKNOWN_POS_ERROR.name(),
                "Invalid transaction"
        ));
        when(authorizationVoidRepositoryPort.save(any(AuthorizationVoid.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateAuthorizationVoidResult result = service.execute(new CreateAuthorizationVoidCommand(
                merchantId,
                paymentIntentId,
                new BigDecimal("1250.50"),
                "TRY"
        ));

        assertThat(result.status()).isEqualTo(AuthorizationVoidStatus.FAILED);
        assertThat(result.failureCode()).isEqualTo(PaymentFailureCode.UNKNOWN_POS_ERROR.name());
        assertThat(result.failureMessage()).isEqualTo("Invalid transaction");
        assertThat(result.posResponseCode()).isEqualTo("12");
        assertThat(result.failedAt()).isEqualTo(NOW);
        assertThat(paymentIntent.getStatus()).isEqualTo(PaymentIntentStatus.REQUIRES_CAPTURE);
        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.AUTHORIZED);
    }

    @Test
    void shouldRejectAmountMismatchBeforeCallingPos() {
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();

        when(paymentIntentRepositoryPort.findById(paymentIntentId))
                .thenReturn(Optional.of(manualIntent(merchantId, paymentIntentId, paymentAttemptId)));
        when(paymentAttemptPort.findById(paymentAttemptId))
                .thenReturn(Optional.of(authorizedAttempt(paymentIntentId, paymentAttemptId)));

        assertThatThrownBy(() -> service.execute(new CreateAuthorizationVoidCommand(
                merchantId,
                paymentIntentId,
                new BigDecimal("1000.00"),
                "TRY"
        ))).isInstanceOf(AuthorizationVoidCannotBeCreatedException.class)
                .hasMessage("Authorization void amount must equal payment intent amount.");

        verify(paymentProcessorPort, never()).voidAuthorization(any(PaymentVoidRequest.class));
        verify(authorizationVoidRepositoryPort, never()).save(any(AuthorizationVoid.class));
    }

    @Test
    void shouldRejectVoidWhenPaymentIntentHasActiveCapture() {
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();

        when(paymentIntentRepositoryPort.findById(paymentIntentId))
                .thenReturn(Optional.of(manualIntent(merchantId, paymentIntentId, paymentAttemptId)));
        when(paymentAttemptPort.findById(paymentAttemptId))
                .thenReturn(Optional.of(authorizedAttempt(paymentIntentId, paymentAttemptId)));
        when(captureRepositoryPort.existsByPaymentIntentIdAndStatus(paymentIntentId, CaptureStatus.PROCESSING)).thenReturn(true);

        assertThatThrownBy(() -> service.execute(new CreateAuthorizationVoidCommand(
                merchantId,
                paymentIntentId,
                new BigDecimal("1250.50"),
                "TRY"
        ))).isInstanceOf(AuthorizationVoidCannotBeCreatedException.class)
                .hasMessage("Payment intent already has a capture and cannot be voided.");

        verify(paymentProcessorPort, never()).voidAuthorization(any(PaymentVoidRequest.class));
        verify(authorizationVoidRepositoryPort, never()).save(any(AuthorizationVoid.class));
    }

    private PaymentIntent manualIntent(UUID merchantId, UUID paymentIntentId, UUID paymentAttemptId) {
        return PaymentIntent.restore(
                paymentIntentId,
                merchantId,
                new BigDecimal("1250.50"),
                Currency.getInstance("TRY"),
                CaptureMethod.MANUAL,
                "order-123",
                "manual void payment",
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
