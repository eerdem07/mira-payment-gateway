package com.eerdem07.mira.gateway.merchants.rest;

import com.eerdem07.mira.gateway.merchants.application.port.in.GenerateApiKeyUseCase;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/v1/apikey")
public class GenerateApikeyMerchantController {
    private final GenerateApiKeyUseCase GenerateApiKeyService;

    public GenerateApikeyMerchantController(GenerateApiKeyUseCase generateApiKeyUseCase) {
        this.GenerateApiKeyService = generateApiKeyUseCase;
    }

    @PostMapping
    public void generateApiKey(@AuthenticationPrincipal Jwt jwt) {
        String uuid = jwt.getSubject();
        this.GenerateApiKeyService.execute();
    }
}
