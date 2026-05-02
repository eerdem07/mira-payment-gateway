package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.SubmitCheckoutSessionCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.SubmitCheckoutSessionResult;
import com.eerdem07.mira.gateway.payments.application.port.in.SubmitCheckoutSessionUseCase;
import com.eerdem07.mira.gateway.payments.rest.dto.SubmitCheckoutSessionRequest;
import com.eerdem07.mira.gateway.payments.rest.dto.SubmitCheckoutSessionResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/checkout-sessions/{token}/submit")
public class SubmitCheckoutSessionController {

    private final SubmitCheckoutSessionUseCase submitCheckoutSessionUseCase;

    public SubmitCheckoutSessionController(SubmitCheckoutSessionUseCase submitCheckoutSessionUseCase) {
        this.submitCheckoutSessionUseCase = submitCheckoutSessionUseCase;
    }

    @PostMapping
    public ResponseEntity<SubmitCheckoutSessionResponse> submitCheckoutSession(
            @PathVariable String token,
            @Valid @RequestBody SubmitCheckoutSessionRequest request) {

        SubmitCheckoutSessionCommand command = new SubmitCheckoutSessionCommand(
                token,
                request.cardNumber(),
                request.expiryMonth(),
                request.expiryYear(),
                request.cvc(),
                request.cardHolderName()
        );

        SubmitCheckoutSessionResult result = submitCheckoutSessionUseCase.execute(command);

        SubmitCheckoutSessionResponse response = new SubmitCheckoutSessionResponse(
                result.checkoutSessionId(),
                result.paymentIntentId(),
                result.checkoutSessionStatus(),
                result.paymentIntentStatus(),
                result.returnUrl(),
                result.failureCode(),
                result.failureMessage()
        );

        return ResponseEntity.ok(response);
    }
}
