package com.eerdem07.mira.gateway.merchants.rest;

import com.eerdem07.mira.gateway.merchants.application.port.in.GenerateApiKeyUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/v1/apikey")
public class GenerateApikeyMerchantController {
    private final GenerateApiKeyUseCase service;

    public GenerateApikeyMerchantController(GenerateApiKeyUseCase generateApiKeyUseCase) {
        this.service = generateApiKeyUseCase;
    }

    @PostMapping
    public void generateApiKey() {

    }
}
