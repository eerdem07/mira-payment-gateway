package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.CreateRefundCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateRefundResult;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateRefundUseCase;
import com.eerdem07.mira.gateway.payments.rest.dto.CreateRefundRequest;
import com.eerdem07.mira.gateway.payments.rest.dto.CreateRefundResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payment-intents/{paymentIntentId}/refunds")
public class CreateRefundController {

    private final CreateRefundUseCase createRefundUseCase;

    public CreateRefundController(CreateRefundUseCase createRefundUseCase) {
        this.createRefundUseCase = createRefundUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateRefundResponse> createRefund(
            @AuthenticationPrincipal String merchantIdString,
            @PathVariable UUID paymentIntentId,
            @Valid @RequestBody CreateRefundRequest request) {

        UUID merchantId = UUID.fromString(merchantIdString);
        CreateRefundResult result = createRefundUseCase.execute(new CreateRefundCommand(
                merchantId,
                paymentIntentId,
                request.amount(),
                request.currency()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    private CreateRefundResponse toResponse(CreateRefundResult result) {
        return new CreateRefundResponse(
                result.id(),
                result.paymentIntentId(),
                result.paymentAttemptId(),
                result.status(),
                result.amount(),
                result.currency(),
                result.posProvider(),
                result.posRefundId(),
                result.posReference(),
                result.posResponseCode(),
                result.posResponseMessage(),
                result.failureCode(),
                result.failureMessage(),
                result.createdAt(),
                result.updatedAt(),
                result.succeededAt(),
                result.failedAt()
        );
    }
}
