package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.CreateCaptureCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateCaptureResult;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateCaptureUseCase;
import com.eerdem07.mira.gateway.payments.rest.dto.CreateCaptureRequest;
import com.eerdem07.mira.gateway.payments.rest.dto.CreateCaptureResponse;
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
    @RequestMapping("/v1/payment-intents/{paymentIntentId}/captures")
public class CreateCaptureController {

    private final CreateCaptureUseCase createCaptureUseCase;

    public CreateCaptureController(CreateCaptureUseCase createCaptureUseCase) {
        this.createCaptureUseCase = createCaptureUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateCaptureResponse> createCapture(
            @AuthenticationPrincipal String merchantIdString,
            @PathVariable UUID paymentIntentId,
            @Valid @RequestBody CreateCaptureRequest request) {

        UUID merchantId = UUID.fromString(merchantIdString);
        CreateCaptureResult result = createCaptureUseCase.execute(new CreateCaptureCommand(
                merchantId,
                paymentIntentId,
                request.amount(),
                request.currency()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    private CreateCaptureResponse toResponse(CreateCaptureResult result) {
        return new CreateCaptureResponse(
                result.id(),
                result.paymentIntentId(),
                result.paymentAttemptId(),
                result.status(),
                result.amount(),
                result.currency(),
                result.posProvider(),
                result.posCaptureId(),
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
