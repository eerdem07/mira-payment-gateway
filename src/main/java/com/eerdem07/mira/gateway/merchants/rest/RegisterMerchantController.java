package com.eerdem07.mira.gateway.merchants.rest;

import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantCommand;
import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantResult;
import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantUseCase;
import com.eerdem07.mira.gateway.merchants.rest.dto.RegisterMerchantRequest;
import com.eerdem07.mira.gateway.merchants.rest.dto.RegisterMerchantResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/merchants")
public class RegisterMerchantController {
    private final RegisterMerchantUseCase registerMerchantService;

    public RegisterMerchantController(RegisterMerchantUseCase registerMerchantUseCase) {
        this.registerMerchantService = registerMerchantUseCase;
    }

    @PostMapping
    public ResponseEntity<RegisterMerchantResponse> register(@Valid @RequestBody RegisterMerchantRequest request) {
        RegisterMerchantResult result = this.registerMerchantService.execute(new RegisterMerchantCommand(request.legalName(), request.email(), request.password(), request.passwordConfirm()));
        RegisterMerchantResponse response = new RegisterMerchantResponse(result.merchantId());
        return ResponseEntity.ok(response);
    }
}
