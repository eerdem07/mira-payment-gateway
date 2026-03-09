package com.eerdem07.mira.gateway.merchants.rest;

import com.eerdem07.mira.gateway.merchants.application.port.in.GenerateApiKeyUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/v1/apikey")
public class GenerateApiKeyController {
    private final GenerateApiKeyUseCase service;

    public GenerateApiKeyController(GenerateApiKeyUseCase generateApiKeyUseCase) {
        this.service = generateApiKeyUseCase;
    }

    @GetMapping
    public void generateApiKey() {

    }
}
