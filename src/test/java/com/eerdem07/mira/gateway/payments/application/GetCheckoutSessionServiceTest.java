package com.eerdem07.mira.gateway.payments.application;

import com.eerdem07.mira.gateway.payments.application.exception.CheckoutSessionNotFoundException;
import com.eerdem07.mira.gateway.payments.application.exception.PaymentIntentNotFoundException;
import com.eerdem07.mira.gateway.payments.application.port.in.GetCheckoutSessionQuery;
import com.eerdem07.mira.gateway.payments.application.port.in.GetCheckoutSessionResult;
import com.eerdem07.mira.gateway.payments.application.port.out.CheckoutSessionRepositoryPort;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentIntentRepositoryPort;
import com.eerdem07.mira.gateway.payments.domain.CheckoutSession;
import com.eerdem07.mira.gateway.payments.domain.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetCheckoutSessionServiceTest {

    private CheckoutSessionRepositoryPort checkoutSessionRepositoryPort;
    private PaymentIntentRepositoryPort paymentIntentRepositoryPort;
    private GetCheckoutSessionService getCheckoutSessionService;

    @BeforeEach
    void setUp() {
        checkoutSessionRepositoryPort = mock(CheckoutSessionRepositoryPort.class);
        paymentIntentRepositoryPort = mock(PaymentIntentRepositoryPort.class);
        getCheckoutSessionService = new GetCheckoutSessionService(checkoutSessionRepositoryPort, paymentIntentRepositoryPort);
    }

    @Test
    void getCheckoutSession_shouldReturnResult_whenSessionAndIntentExist() {
        String token = "valid-token";
        UUID paymentIntentId = UUID.randomUUID();
        CheckoutSession session = CheckoutSession.create(
                UUID.randomUUID(), paymentIntentId, token, 
                "http://return.url", "http://cancel.url", 
                Instant.now().plusSeconds(3600), Instant.now()
        );
        
        PaymentIntent intent = PaymentIntent.create(
                paymentIntentId, UUID.randomUUID(), BigDecimal.valueOf(100.00), Currency.getInstance("TRY"),
                "merchant-ref", "Premium plan payment", Instant.now(), Instant.now().plusSeconds(3600)
        );

        when(checkoutSessionRepositoryPort.findByToken(token)).thenReturn(Optional.of(session));
        when(paymentIntentRepositoryPort.findById(paymentIntentId)).thenReturn(Optional.of(intent));

        GetCheckoutSessionResult result = getCheckoutSessionService.getCheckoutSession(new GetCheckoutSessionQuery(token));

        assertNotNull(result);
        assertEquals(session.getId().toString(), result.id());
        assertEquals("OPEN", result.status());
        assertEquals(session.getExpiresAt(), result.expiresAt());
        assertNotNull(result.payment());
        assertEquals(BigDecimal.valueOf(100.00), result.payment().amount());
        assertEquals("TRY", result.payment().currency());
        assertEquals("Premium plan payment", result.payment().description());
    }

    @Test
    void getCheckoutSession_shouldThrowException_whenSessionNotFound() {
        String token = "invalid-token";
        when(checkoutSessionRepositoryPort.findByToken(token)).thenReturn(Optional.empty());

        assertThrows(CheckoutSessionNotFoundException.class, () -> 
                getCheckoutSessionService.getCheckoutSession(new GetCheckoutSessionQuery(token)));
    }

    @Test
    void getCheckoutSession_shouldThrowException_whenIntentNotFound() {
        String token = "valid-token";
        UUID paymentIntentId = UUID.randomUUID();
        CheckoutSession session = CheckoutSession.create(
                UUID.randomUUID(), paymentIntentId, token, 
                "http://return.url", "http://cancel.url", 
                Instant.now().plusSeconds(3600), Instant.now()
        );

        when(checkoutSessionRepositoryPort.findByToken(token)).thenReturn(Optional.of(session));
        when(paymentIntentRepositoryPort.findById(paymentIntentId)).thenReturn(Optional.empty());

        assertThrows(PaymentIntentNotFoundException.class, () -> 
                getCheckoutSessionService.getCheckoutSession(new GetCheckoutSessionQuery(token)));
    }
}
