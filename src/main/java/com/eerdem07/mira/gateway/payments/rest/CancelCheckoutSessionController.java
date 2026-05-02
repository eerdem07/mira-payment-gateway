package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.CancelCheckoutSessionCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CancelCheckoutSessionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/checkout-sessions/{token}/cancel")
public class CancelCheckoutSessionController {

    private final CancelCheckoutSessionUseCase cancelCheckoutSessionUseCase;

    public CancelCheckoutSessionController(CancelCheckoutSessionUseCase cancelCheckoutSessionUseCase) {
        this.cancelCheckoutSessionUseCase = cancelCheckoutSessionUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> cancelCheckoutSession(@PathVariable String token) {
        var command = new CancelCheckoutSessionCommand(token);
        cancelCheckoutSessionUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }
}
