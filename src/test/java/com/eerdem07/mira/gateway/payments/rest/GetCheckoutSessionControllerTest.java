package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.GetCheckoutSessionQuery;
import com.eerdem07.mira.gateway.payments.application.port.in.GetCheckoutSessionResult;
import com.eerdem07.mira.gateway.payments.application.port.in.GetCheckoutSessionUseCase;
import com.eerdem07.mira.gateway.payments.rest.dto.GetCheckoutSessionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetCheckoutSessionControllerTest {

    @Test
    void getCheckoutSession_shouldReturnOk() {
        GetCheckoutSessionUseCase useCase = mock(GetCheckoutSessionUseCase.class);
        GetCheckoutSessionController controller = new GetCheckoutSessionController(useCase);

        String token = "test-token";
        GetCheckoutSessionResult result = new GetCheckoutSessionResult(
                "id", "OPEN", Instant.now(), "https://return.url", "https://cancel.url",
                new GetCheckoutSessionResult.PaymentDetails(BigDecimal.TEN, "TRY", "desc")
        );

        when(useCase.getCheckoutSession(new GetCheckoutSessionQuery(token))).thenReturn(result);

        ResponseEntity<GetCheckoutSessionResponse> response = controller.getCheckoutSession(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("id", response.getBody().id());
        assertEquals("OPEN", response.getBody().status());
        assertEquals("https://return.url", response.getBody().returnUrl());
        assertEquals("https://cancel.url", response.getBody().cancelUrl());
        assertEquals(BigDecimal.TEN, response.getBody().payment().amount());
        assertEquals("TRY", response.getBody().payment().currency());
        assertEquals("desc", response.getBody().payment().description());
    }
}
