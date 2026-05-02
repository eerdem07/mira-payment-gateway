package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.GetCheckoutSessionQuery;
import com.eerdem07.mira.gateway.payments.application.port.in.GetCheckoutSessionResult;
import com.eerdem07.mira.gateway.payments.application.port.in.GetCheckoutSessionUseCase;
import com.eerdem07.mira.gateway.payments.rest.dto.GetCheckoutSessionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/checkout-sessions")
public class GetCheckoutSessionController {

    private final GetCheckoutSessionUseCase getCheckoutSessionUseCase;

    public GetCheckoutSessionController(GetCheckoutSessionUseCase getCheckoutSessionUseCase) {
        this.getCheckoutSessionUseCase = getCheckoutSessionUseCase;
    }

    @GetMapping("/{token}")
    public ResponseEntity<GetCheckoutSessionResponse> getCheckoutSession(@PathVariable String token) {
        GetCheckoutSessionQuery query = new GetCheckoutSessionQuery(token);
        GetCheckoutSessionResult result = getCheckoutSessionUseCase.getCheckoutSession(query);
        
        GetCheckoutSessionResponse response = new GetCheckoutSessionResponse(
                result.id(),
                result.status(),
                result.expiresAt(),
                result.returnUrl(),
                result.cancelUrl(),
                new GetCheckoutSessionResponse.PaymentDetails(
                        result.payment().amount(),
                        result.payment().currency(),
                        result.payment().description()
                )
        );
        
        return ResponseEntity.ok(response);
    }
}
