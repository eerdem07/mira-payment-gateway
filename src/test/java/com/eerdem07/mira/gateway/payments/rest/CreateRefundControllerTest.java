package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.CreateRefundCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateRefundResult;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateRefundUseCase;
import com.eerdem07.mira.gateway.payments.domain.RefundStatus;
import com.eerdem07.mira.gateway.payments.rest.dto.CreateRefundRequest;
import com.eerdem07.mira.gateway.payments.rest.dto.CreateRefundResponse;
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

class CreateRefundControllerTest {

    @Test
    void createRefund_shouldReturnCreatedRefund() {
        CreateRefundUseCase useCase = mock(CreateRefundUseCase.class);
        CreateRefundController controller = new CreateRefundController(useCase);
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        UUID paymentAttemptId = UUID.randomUUID();
        UUID refundId = UUID.randomUUID();
        Instant now = Instant.parse("2026-05-13T09:00:00Z");

        when(useCase.execute(new CreateRefundCommand(
                merchantId,
                paymentIntentId,
                new BigDecimal("1250.50"),
                "TRY"
        ))).thenReturn(new CreateRefundResult(
                refundId,
                paymentIntentId,
                paymentAttemptId,
                RefundStatus.SUCCEEDED,
                new BigDecimal("1250.50"),
                "TRY",
                "MOCK_BANK_POS",
                "pos_ref_123",
                "HST-REF-123",
                "00",
                "Refund approved",
                null,
                null,
                now,
                now,
                now,
                null
        ));

        ResponseEntity<CreateRefundResponse> response = controller.createRefund(
                merchantId.toString(),
                paymentIntentId,
                new CreateRefundRequest(new BigDecimal("1250.50"), "TRY")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(refundId);
        assertThat(response.getBody().status()).isEqualTo(RefundStatus.SUCCEEDED);
        assertThat(response.getBody().posRefundId()).isEqualTo("pos_ref_123");
        assertThat(response.getBody().posResponseCode()).isEqualTo("00");

        ArgumentCaptor<CreateRefundCommand> commandCaptor = ArgumentCaptor.forClass(CreateRefundCommand.class);
        verify(useCase).execute(commandCaptor.capture());
        assertThat(commandCaptor.getValue().merchantId()).isEqualTo(merchantId);
        assertThat(commandCaptor.getValue().paymentIntentId()).isEqualTo(paymentIntentId);
    }
}
