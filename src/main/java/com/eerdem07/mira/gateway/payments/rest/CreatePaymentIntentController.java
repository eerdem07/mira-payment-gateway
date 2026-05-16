package com.eerdem07.mira.gateway.payments.rest;

import com.eerdem07.mira.gateway.payments.application.port.in.CreatePaymentIntentCommand;
import com.eerdem07.mira.gateway.payments.application.port.in.CreatePaymentIntentUseCase;
import com.eerdem07.mira.gateway.payments.rest.dto.CreatePaymentIntentRequest;
import com.eerdem07.mira.gateway.payments.rest.dto.CreatePaymentIntentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payment-intents")
public class CreatePaymentIntentController {
    private final CreatePaymentIntentUseCase createPaymentIntentUseCase;

    public CreatePaymentIntentController(CreatePaymentIntentUseCase createPaymentIntentUseCase){
        this.createPaymentIntentUseCase = createPaymentIntentUseCase;
    }


    @PostMapping
    public ResponseEntity<CreatePaymentIntentResponse> createPaymentIntent(
            @AuthenticationPrincipal String merchantIdString,
            @Valid @RequestBody CreatePaymentIntentRequest request){
        
        UUID merchantId = UUID.fromString(merchantIdString);
        var result = this.createPaymentIntentUseCase.execute(new CreatePaymentIntentCommand(
                merchantId,
                request.amount(),
                request.currency(),
                request.captureMethod(),
                request.merchantReference(),
                request.description()
        ));
        CreatePaymentIntentResponse response = new CreatePaymentIntentResponse(
                result.id(),
                result.status(),
                result.amount(),
                result.currency(),
                result.captureMethod(),
                result.merchantReference(),
                result.description(),
                result.expiresAt(),
                result.createdAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
