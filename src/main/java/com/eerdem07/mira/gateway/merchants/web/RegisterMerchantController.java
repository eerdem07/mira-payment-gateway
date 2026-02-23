package com.eerdem07.mira.gateway.merchants.web;

import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantCommand;
import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantResult;
import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantUseCase;
import com.eerdem07.mira.gateway.merchants.web.dto.RegisterMerchantRequest;
import com.eerdem07.mira.gateway.merchants.web.dto.RegisterMerchantResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/v1/merchants")
public class RegisterMerchantController {
    private final RegisterMerchantUseCase registerMerchant;

    public RegisterMerchantController(RegisterMerchantUseCase registerMerchant) {
        this.registerMerchant = registerMerchant;
    }

    @PostMapping
    public ResponseEntity<RegisterMerchantResponse> register(@Valid @RequestBody RegisterMerchantRequest request) {
        RegisterMerchantResult result = registerMerchant.execute(new RegisterMerchantCommand(request.legalName(), request.email(), request.password(), request.passwordConfirm()));
        RegisterMerchantResponse response = new RegisterMerchantResponse(result.merchantId());

        return ResponseEntity.created(URI.create("/v1/merchants/" + response.merchantId()))
                .body(response);
    }
}
