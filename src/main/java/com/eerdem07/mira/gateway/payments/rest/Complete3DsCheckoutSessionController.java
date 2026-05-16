package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.Complete3DsCheckoutSessionCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.Complete3DsCheckoutSessionResult;
import com.eerdem07.mira.gateway.payments.application.port.in.Complete3DsCheckoutSessionUseCase;
import com.eerdem07.mira.gateway.payments.rest.dto.Complete3DsCheckoutSessionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/checkout-sessions/{token}/3ds/complete")
public class Complete3DsCheckoutSessionController {

    private final Complete3DsCheckoutSessionUseCase complete3DsCheckoutSessionUseCase;

    public Complete3DsCheckoutSessionController(Complete3DsCheckoutSessionUseCase complete3DsCheckoutSessionUseCase) {
        this.complete3DsCheckoutSessionUseCase = complete3DsCheckoutSessionUseCase;
    }

    @PostMapping
    public ResponseEntity<Complete3DsCheckoutSessionResponse> complete3ds(@PathVariable String token) {
        Complete3DsCheckoutSessionCommand command = new Complete3DsCheckoutSessionCommand(token);
        Complete3DsCheckoutSessionResult result = complete3DsCheckoutSessionUseCase.execute(command);

        Complete3DsCheckoutSessionResponse response = new Complete3DsCheckoutSessionResponse(
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
