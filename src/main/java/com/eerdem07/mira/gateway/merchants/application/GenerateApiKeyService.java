package com.eerdem07.mira.gateway.merchants.application;

import com.eerdem07.mira.gateway.merchants.application.port.in.GenerateApiKeyResult;
import com.eerdem07.mira.gateway.merchants.application.port.in.GenerateApiKeyUseCase;
import org.springframework.stereotype.Service;

@Service
public class GenerateApiKeyService implements GenerateApiKeyUseCase {

    @Override
    public GenerateApiKeyResult execute() {
        return new GenerateApiKeyResult();
    }
}
