package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.CreateCaptureCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateCaptureResult;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateCaptureUseCase;
import com.eerdem07.mira.gateway.payments.domain.CaptureStatus;
import com.eerdem07.mira.gateway.payments.rest.dto.CreateCaptureRequest;
import com.eerdem07.mira.gateway.payments.rest.dto.CreateCaptureResponse;
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

class CreateCaptureControllerTest {

    @Test
    void createCapture_shouldReturnCreatedCapture() {
        CreateCaptureUseCase useCase = mock(CreateCaptureUseCase.class);
        CreateCaptureController controller = new CreateCaptureController(useCase);
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();
        UUID captureId = UUID.randomUUID();
        Instant now = Instant.parse("2026-05-13T08:30:00Z");

        when(useCase.execute(new CreateCaptureCommand(
                merchantId,
                paymentIntentId,
                new BigDecimal("1250.50"),
                "TRY"
        ))).thenReturn(new CreateCaptureResult(
                captureId,
                paymentIntentId,
                paymentAttemptId,
                CaptureStatus.SUCCEEDED,
                new BigDecimal("1250.50"),
                "TRY",
                "MOCK_BANK_POS",
                "pos_cap_123",
                "HST-CAP-123",
                "00",
                "Capture approved",
                null,
                null,
                now,
                now,
                now,
                null
        ));

        ResponseEntity<CreateCaptureResponse> response = controller.createCapture(
                merchantId.toString(),
                paymentIntentId,
                new CreateCaptureRequest(new BigDecimal("1250.50"), "TRY")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(captureId);
        assertThat(response.getBody().status()).isEqualTo(CaptureStatus.SUCCEEDED);
        assertThat(response.getBody().posCaptureId()).isEqualTo("pos_cap_123");
        assertThat(response.getBody().posResponseCode()).isEqualTo("00");

        ArgumentCaptor<CreateCaptureCommand> commandCaptor = ArgumentCaptor.forClass(CreateCaptureCommand.class);
        verify(useCase).execute(commandCaptor.capture());
        assertThat(commandCaptor.getValue().merchantId()).isEqualTo(merchantId);
        assertThat(commandCaptor.getValue().paymentIntentId()).isEqualTo(paymentIntentId);
    }
}
