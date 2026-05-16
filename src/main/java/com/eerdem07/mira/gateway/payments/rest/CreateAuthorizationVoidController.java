package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.CreateAuthorizationVoidCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateAuthorizationVoidResult;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateAuthorizationVoidUseCase;
import com.eerdem07.mira.gateway.payments.rest.dto.CreateAuthorizationVoidRequest;
import com.eerdem07.mira.gateway.payments.rest.dto.CreateAuthorizationVoidResponse;
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
@RequestMapping("/v1/payment-intents/{paymentIntentId}/voids")
public class CreateAuthorizationVoidController {

    private final CreateAuthorizationVoidUseCase createAuthorizationVoidUseCase;

    public CreateAuthorizationVoidController(CreateAuthorizationVoidUseCase createAuthorizationVoidUseCase) {
        this.createAuthorizationVoidUseCase = createAuthorizationVoidUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateAuthorizationVoidResponse> createAuthorizationVoid(
            @AuthenticationPrincipal String merchantIdString,
            @PathVariable UUID paymentIntentId,
            @Valid @RequestBody CreateAuthorizationVoidRequest request) {

        UUID merchantId = UUID.fromString(merchantIdString);
        CreateAuthorizationVoidResult result = createAuthorizationVoidUseCase.execute(new CreateAuthorizationVoidCommand(
                merchantId,
                paymentIntentId,
                request.amount(),
                request.currency()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    private CreateAuthorizationVoidResponse toResponse(CreateAuthorizationVoidResult result) {
        return new CreateAuthorizationVoidResponse(
                result.id(),
                result.paymentIntentId(),
                result.paymentAttemptId(),
                result.status(),
                result.amount(),
                result.currency(),
                result.posProvider(),
                result.posVoidId(),
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
