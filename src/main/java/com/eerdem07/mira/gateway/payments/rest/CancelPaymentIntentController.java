package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.CancelPaymentIntentCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CancelPaymentIntentResult;
import com.eerdem07.mira.gateway.payments.application.port.in.CancelPaymentIntentUseCase;
import com.eerdem07.mira.gateway.payments.rest.dto.CancelPaymentIntentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payment-intents")
public class CancelPaymentIntentController {

    private final CancelPaymentIntentUseCase cancelPaymentIntentUseCase;

    public CancelPaymentIntentController(CancelPaymentIntentUseCase cancelPaymentIntentUseCase) {
        this.cancelPaymentIntentUseCase = cancelPaymentIntentUseCase;
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<CancelPaymentIntentResponse> cancel(
            @AuthenticationPrincipal String merchantIdString,
            @PathVariable UUID id) {

        UUID merchantId = UUID.fromString(merchantIdString);
        CancelPaymentIntentResult result = cancelPaymentIntentUseCase.execute(
                new CancelPaymentIntentCommand(merchantId, id)
        );

        return ResponseEntity.ok(new CancelPaymentIntentResponse(result.id(), result.status()));
    }
}
