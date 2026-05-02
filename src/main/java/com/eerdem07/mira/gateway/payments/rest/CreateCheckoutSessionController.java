package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.CreateCheckoutSessionCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreateCheckoutSessionUseCase;
import com.eerdem07.mira.gateway.payments.rest.dto.CreateCheckoutSessionRequest;
import com.eerdem07.mira.gateway.payments.rest.dto.CreateCheckoutSessionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payment-intents/{paymentIntentId}/checkout-sessions")
public class CreateCheckoutSessionController {

    private final CreateCheckoutSessionUseCase createCheckoutSessionUseCase;

    public CreateCheckoutSessionController(CreateCheckoutSessionUseCase createCheckoutSessionUseCase) {
        this.createCheckoutSessionUseCase = createCheckoutSessionUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateCheckoutSessionResponse> createCheckoutSession(
            @PathVariable UUID paymentIntentId,
            @Valid @RequestBody CreateCheckoutSessionRequest request) {

        var command = new CreateCheckoutSessionCommand(
                paymentIntentId,
                request.returnUrl(),
                request.cancelUrl()
        );

        var result = this.createCheckoutSessionUseCase.execute(command);

        var response = new CreateCheckoutSessionResponse(
                result.id(),
                result.paymentIntentId(),
                result.token(),
                result.url(),
                result.status(),
                result.expiresAt(),
                result.createdAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
