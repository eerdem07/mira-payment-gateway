package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.GetPaymentIntentQuery;
import com.eerdem07.mira.gateway.payments.application.port.in.GetPaymentIntentUseCase;
import com.eerdem07.mira.gateway.payments.rest.dto.GetPaymentIntentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payment-intents")
public class GetPaymentIntentController {

    private final GetPaymentIntentUseCase getPaymentIntentUseCase;

    public GetPaymentIntentController(GetPaymentIntentUseCase getPaymentIntentUseCase) {
        this.getPaymentIntentUseCase = getPaymentIntentUseCase;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetPaymentIntentResponse> getPaymentIntent(@PathVariable UUID id) {
        var result = getPaymentIntentUseCase.execute(new GetPaymentIntentQuery(id));
        
        GetPaymentIntentResponse response = new GetPaymentIntentResponse(
                result.id(),
                result.status(),
                result.amount(),
                result.currency(),
                result.merchantReference(),
                result.description(),
                result.expiresAt(),
                result.createdAt()
        );

        return ResponseEntity.ok(response);
    }
}
