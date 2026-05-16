package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.CreateAuthorizationVoidCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateAuthorizationVoidResult;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateAuthorizationVoidUseCase;
import com.eerdem07.mira.gateway.payments.domain.AuthorizationVoidStatus;
import com.eerdem07.mira.gateway.payments.rest.dto.CreateAuthorizationVoidRequest;
import com.eerdem07.mira.gateway.payments.rest.dto.CreateAuthorizationVoidResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateAuthorizationVoidControllerTest {

    @Test
    void createAuthorizationVoid_shouldReturnCreatedAuthorizationVoid() {
        CreateAuthorizationVoidUseCase useCase = mock(CreateAuthorizationVoidUseCase.class);
        CreateAuthorizationVoidController controller = new CreateAuthorizationVoidController(useCase);
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();
        UUID voidId = UUID.randomUUID();
        Instant now = Instant.parse("2026-05-13T08:45:00Z");

        when(useCase.execute(new CreateAuthorizationVoidCommand(
                merchantId,
                paymentIntentId,
                new BigDecimal("1250.50"),
                "TRY"
        ))).thenReturn(new CreateAuthorizationVoidResult(
                voidId,
                paymentIntentId,
                paymentAttemptId,
                AuthorizationVoidStatus.SUCCEEDED,
                new BigDecimal("1250.50"),
                "TRY",
                "MOCK_BANK_POS",
                "pos_void_123",
                "HST-VOID-123",
                "00",
                "Void approved",
                null,
                null,
                now,
                now,
                now,
                null
        ));

        ResponseEntity<CreateAuthorizationVoidResponse> response = controller.createAuthorizationVoid(
                merchantId.toString(),
                paymentIntentId,
                new CreateAuthorizationVoidRequest(new BigDecimal("1250.50"), "TRY")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(voidId);
        assertThat(response.getBody().status()).isEqualTo(AuthorizationVoidStatus.SUCCEEDED);
        assertThat(response.getBody().posVoidId()).isEqualTo("pos_void_123");
        assertThat(response.getBody().posResponseCode()).isEqualTo("00");

        ArgumentCaptor<CreateAuthorizationVoidCommand> commandCaptor = ArgumentCaptor.forClass(CreateAuthorizationVoidCommand.class);
        verify(useCase).execute(commandCaptor.capture());
        assertThat(commandCaptor.getValue().merchantId()).isEqualTo(merchantId);
        assertThat(commandCaptor.getValue().paymentIntentId()).isEqualTo(paymentIntentId);
    }
}
