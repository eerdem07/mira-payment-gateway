package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.CheckoutSessionNotFoundException;
import com.eerdem07.mira.gateway.payments.application.exception.PaymentAttemptNotFoundException;
import com.eerdem07.mira.gateway.payments.application.port.in.Complete3DsCheckoutSessionCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.Complete3DsCheckoutSessionResult;
import com.eerdem07.mira.gateway.payments.application.port.out.CheckoutSessionRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAttemptPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAuthorizationResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAuthorizationStatus;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentProcessorPort;
import com.eerdem07.mira.gateway.payments.application.port.out.ThreeDsCompleteRequest;
import com.eerdem07.mira.gateway.payments.domain.CaptureMethod;
import com.eerdem07.mira.gateway.payments.domain.CheckoutSession;
import com.eerdem07.mira.gateway.payments.domain.CheckoutSessionStatus;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttempt;
import com.eerdem07.mira.gateway.payments.domain.PaymentAttemptStatus;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Complete3DsCheckoutSessionServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-16T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private final CheckoutSessionRepositoryPort checkoutSessionRepositoryPort = mock(CheckoutSessionRepositoryPort.class);
    private final PaymentIntentRepositoryPort paymentIntentRepositoryPort = mock(PaymentIntentRepositoryPort.class);
    private final PaymentAttemptPort paymentAttemptPort = mock(PaymentAttemptPort.class);
    private final PaymentProcessorPort paymentProcessorPort = mock(PaymentProcessorPort.class);

    private final Complete3DsCheckoutSessionService service = new Complete3DsCheckoutSessionService(
            checkoutSessionRepositoryPort,
            paymentIntentRepositoryPort,
            paymentAttemptPort,
            paymentProcessorPort,
            CLOCK
    );

    @Test
    void shouldAuthorizeAndRequireCaptureWhenChallengeApprovedAndManualCapture() {
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();
        String threeDsSessionId = "3ds_abc123";

        CheckoutSession checkoutSession = actionRequiredSession(paymentIntentId);
        PaymentIntent paymentIntent = requiresActionIntent(paymentIntentId, CaptureMethod.MANUAL);
        PaymentAttempt paymentAttempt = requiresActionAttempt(paymentIntentId, paymentAttemptId, threeDsSessionId);

        stubLookups(checkoutSession, paymentIntent, paymentAttempt);
        when(paymentProcessorPort.complete3ds(any())).thenReturn(authorizedResult());
        stubSaves();

        Complete3DsCheckoutSessionResult result = service.execute(new Complete3DsCheckoutSessionCommand("test-token"));

        assertThat(result.paymentIntentStatus()).isEqualTo(PaymentIntentStatus.REQUIRES_CAPTURE);
        assertThat(result.checkoutSessionStatus()).isEqualTo(CheckoutSessionStatus.SUBMITTED);
        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.AUTHORIZED);
        assertThat(paymentAttempt.getPosTransactionId()).isEqualTo("pos_txn_complete");
        assertThat(paymentAttempt.getPosAuthCode()).isEqualTo("AUTH456");
        assertThat(paymentAttempt.getAuthorizedAt()).isEqualTo(NOW);
        assertThat(paymentIntent.getStatus()).isEqualTo(PaymentIntentStatus.REQUIRES_CAPTURE);
    }

    @Test
    void shouldSucceedWhenChallengeApprovedAndAutomaticCapture() {
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();
        String threeDsSessionId = "3ds_abc123";

        CheckoutSession checkoutSession = actionRequiredSession(paymentIntentId);
        PaymentIntent paymentIntent = requiresActionIntent(paymentIntentId, CaptureMethod.AUTOMATIC);
        PaymentAttempt paymentAttempt = requiresActionAttempt(paymentIntentId, paymentAttemptId, threeDsSessionId);

        stubLookups(checkoutSession, paymentIntent, paymentAttempt);
        when(paymentProcessorPort.complete3ds(any())).thenReturn(authorizedResult());
        stubSaves();

        Complete3DsCheckoutSessionResult result = service.execute(new Complete3DsCheckoutSessionCommand("test-token"));

        assertThat(result.paymentIntentStatus()).isEqualTo(PaymentIntentStatus.SUCCEEDED);
        assertThat(result.checkoutSessionStatus()).isEqualTo(CheckoutSessionStatus.SUBMITTED);
        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.SUCCEEDED);
        assertThat(paymentAttempt.getSucceededAt()).isEqualTo(NOW);
        assertThat(paymentIntent.getStatus()).isEqualTo(PaymentIntentStatus.SUCCEEDED);
    }

    @Test
    void shouldDeclineAttemptAndRequireNewPaymentMethodWhenThreeDsAuthFails() {
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();

        CheckoutSession checkoutSession = actionRequiredSession(paymentIntentId);
        PaymentIntent paymentIntent = requiresActionIntent(paymentIntentId, CaptureMethod.AUTOMATIC);
        PaymentAttempt paymentAttempt = requiresActionAttempt(paymentIntentId, paymentAttemptId, "3ds_abc123");

        stubLookups(checkoutSession, paymentIntent, paymentAttempt);
        when(paymentProcessorPort.complete3ds(any())).thenReturn(declinedResult());
        stubSaves();

        Complete3DsCheckoutSessionResult result = service.execute(new Complete3DsCheckoutSessionCommand("test-token"));

        assertThat(result.paymentIntentStatus()).isEqualTo(PaymentIntentStatus.REQUIRES_PAYMENT_METHOD);
        assertThat(result.checkoutSessionStatus()).isEqualTo(CheckoutSessionStatus.SUBMITTED);
        assertThat(result.failureCode()).isNotBlank();
        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.DECLINED);
        assertThat(paymentAttempt.getDeclinedAt()).isEqualTo(NOW);
        assertThat(paymentIntent.getStatus()).isEqualTo(PaymentIntentStatus.REQUIRES_PAYMENT_METHOD);
    }

    @Test
    void shouldFailAttemptAndIntentWhenPosReturnsError() {
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();

        CheckoutSession checkoutSession = actionRequiredSession(paymentIntentId);
        PaymentIntent paymentIntent = requiresActionIntent(paymentIntentId, CaptureMethod.AUTOMATIC);
        PaymentAttempt paymentAttempt = requiresActionAttempt(paymentIntentId, paymentAttemptId, "3ds_timeout_999");

        stubLookups(checkoutSession, paymentIntent, paymentAttempt);
        when(paymentProcessorPort.complete3ds(any())).thenReturn(errorResult());
        stubSaves();

        Complete3DsCheckoutSessionResult result = service.execute(new Complete3DsCheckoutSessionCommand("test-token"));

        assertThat(result.paymentIntentStatus()).isEqualTo(PaymentIntentStatus.FAILED);
        assertThat(result.checkoutSessionStatus()).isEqualTo(CheckoutSessionStatus.SUBMITTED);
        assertThat(result.failureCode()).isNotBlank();
        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
        assertThat(paymentAttempt.getFailedAt()).isEqualTo(NOW);
        assertThat(paymentIntent.getStatus()).isEqualTo(PaymentIntentStatus.FAILED);
    }

    @Test
    void shouldPassThreeDsSessionIdFromAttemptToPos() {
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();
        String threeDsSessionId = "3ds_specific_session_id";

        CheckoutSession checkoutSession = actionRequiredSession(paymentIntentId);
        PaymentIntent paymentIntent = requiresActionIntent(paymentIntentId, CaptureMethod.AUTOMATIC);
        PaymentAttempt paymentAttempt = requiresActionAttempt(paymentIntentId, paymentAttemptId, threeDsSessionId);

        stubLookups(checkoutSession, paymentIntent, paymentAttempt);
        when(paymentProcessorPort.complete3ds(any())).thenReturn(authorizedResult());
        stubSaves();

        service.execute(new Complete3DsCheckoutSessionCommand("test-token"));

        ArgumentCaptor<ThreeDsCompleteRequest> captor = ArgumentCaptor.forClass(ThreeDsCompleteRequest.class);
        verify(paymentProcessorPort).complete3ds(captor.capture());
        assertThat(captor.getValue().threeDsSessionId()).isEqualTo(threeDsSessionId);
        assertThat(captor.getValue().paymentIntentId()).isEqualTo(paymentIntentId);
        assertThat(captor.getValue().paymentAttemptId()).isEqualTo(paymentAttemptId);
    }

    @Test
    void shouldThrowWhenCheckoutSessionNotFound() {
        when(checkoutSessionRepositoryPort.findByToken("missing-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new Complete3DsCheckoutSessionCommand("missing-token")))
                .isInstanceOf(CheckoutSessionNotFoundException.class);

        verify(paymentProcessorPort, never()).complete3ds(any());
    }

    @Test
    void shouldThrowWhenNoRequiresActionAttemptExists() {
        UUID paymentIntentId = UUID.randomUUID();
        CheckoutSession checkoutSession = actionRequiredSession(paymentIntentId);
        PaymentIntent paymentIntent = requiresActionIntent(paymentIntentId, CaptureMethod.AUTOMATIC);

        when(checkoutSessionRepositoryPort.findByToken("test-token")).thenReturn(Optional.of(checkoutSession));
        when(paymentIntentRepositoryPort.findById(paymentIntentId)).thenReturn(Optional.of(paymentIntent));
        when(paymentAttemptPort.findLatestByPaymentIntentIdAndStatus(paymentIntentId, PaymentAttemptStatus.REQUIRES_ACTION))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new Complete3DsCheckoutSessionCommand("test-token")))
                .isInstanceOf(PaymentAttemptNotFoundException.class);

        verify(paymentProcessorPort, never()).complete3ds(any());
    }

    @Test
    void shouldSubmitCheckoutSessionAfterSuccessfulAuthorization() {
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();

        CheckoutSession checkoutSession = actionRequiredSession(paymentIntentId);
        PaymentIntent paymentIntent = requiresActionIntent(paymentIntentId, CaptureMethod.AUTOMATIC);
        PaymentAttempt paymentAttempt = requiresActionAttempt(paymentIntentId, paymentAttemptId, "3ds_abc");

        stubLookups(checkoutSession, paymentIntent, paymentAttempt);
        when(paymentProcessorPort.complete3ds(any())).thenReturn(authorizedResult());
        stubSaves();

        service.execute(new Complete3DsCheckoutSessionCommand("test-token"));

        assertThat(checkoutSession.getStatus()).isEqualTo(CheckoutSessionStatus.SUBMITTED);
        assertThat(checkoutSession.getSubmittedAt()).isEqualTo(NOW);
    }

    private void stubLookups(CheckoutSession checkoutSession, PaymentIntent paymentIntent, PaymentAttempt paymentAttempt) {
        when(checkoutSessionRepositoryPort.findByToken("test-token")).thenReturn(Optional.of(checkoutSession));
        when(paymentIntentRepositoryPort.findById(paymentIntent.getId())).thenReturn(Optional.of(paymentIntent));
        when(paymentAttemptPort.findLatestByPaymentIntentIdAndStatus(
                eq(paymentIntent.getId()), eq(PaymentAttemptStatus.REQUIRES_ACTION)))
                .thenReturn(Optional.of(paymentAttempt));
    }

    private void stubSaves() {
        when(checkoutSessionRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentIntentRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentAttemptPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private CheckoutSession actionRequiredSession(UUID paymentIntentId) {
        return CheckoutSession.restore(
                UUID.randomUUID(),
                paymentIntentId,
                "test-token",
                CheckoutSessionStatus.ACTION_REQUIRED,
                "https://example.com/return",
                "https://example.com/cancel",
                NOW.plusSeconds(3600),
                NOW.minusSeconds(60),
                NOW.minusSeconds(30),
                null,
                null
        );
    }

    private PaymentIntent requiresActionIntent(UUID paymentIntentId, CaptureMethod captureMethod) {
        return PaymentIntent.restore(
                paymentIntentId,
                UUID.randomUUID(),
                new BigDecimal("500.00"),
                Currency.getInstance("TRY"),
                captureMethod,
                "order-3ds-001",
                "3DS payment",
                PaymentIntentStatus.REQUIRES_ACTION,
                1,
                null,
                null,
                NOW.plusSeconds(3600),
                null,
                null,
                NOW.minusSeconds(60),
                NOW.minusSeconds(30),
                null,
                null
        );
    }

    private PaymentAttempt requiresActionAttempt(UUID paymentIntentId, UUID paymentAttemptId, String threeDsSessionId) {
        return PaymentAttempt.restore(
                paymentAttemptId,
                paymentIntentId,
                UUID.randomUUID(),
                PaymentAttemptStatus.REQUIRES_ACTION,
                new BigDecimal("500.00"),
                Currency.getInstance("TRY"),
                "VISA",
                "3006",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                threeDsSessionId,
                "CHALLENGE",
                NOW.minusSeconds(60),
                NOW.minusSeconds(30),
                NOW.minusSeconds(50),
                NOW.minusSeconds(30),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private PaymentAuthorizationResult authorizedResult() {
        return new PaymentAuthorizationResult(
                PaymentAuthorizationStatus.AUTHORIZED,
                "MOCK_BANK_POS",
                "pos_txn_complete",
                "AUTH456",
                "HST-456",
                "00",
                "Authorized",
                null,
                null,
                null,
                null,
                null
        );
    }

    private PaymentAuthorizationResult declinedResult() {
        return new PaymentAuthorizationResult(
                PaymentAuthorizationStatus.DECLINED,
                "MOCK_BANK_POS",
                null,
                null,
                null,
                "3DS_AUTH_FAILED",
                "3DS authentication failed",
                "UNKNOWN_POS_ERROR",
                "3DS authentication failed",
                null,
                null,
                null
        );
    }

    private PaymentAuthorizationResult errorResult() {
        return new PaymentAuthorizationResult(
                PaymentAuthorizationStatus.ERROR,
                "MOCK_BANK_POS",
                null,
                null,
                null,
                "3DS_TIMEOUT",
                "3DS session expired",
                "POS_TIMEOUT",
                "3DS session expired",
                null,
                null,
                null
        );
    }
}
