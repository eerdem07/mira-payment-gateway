package com.eerdem07.mira.gateway.merchants.rest;

import com.eerdem07.mira.gateway.merchants.application.port.in.GenerateApiKeyCommand;
import com.eerdem07.mira.gateway.merchants.application.port.in.GenerateApiKeyUseCase;
import com.eerdem07.mira.gateway.shared.exception.MissingJwtClaimException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/apikeys")
public class GenerateApiKeyMerchantController {
    private final GenerateApiKeyUseCase generateApiKeyService;

    public GenerateApiKeyMerchantController(GenerateApiKeyUseCase generateApiKeyUseCase) {
        this.generateApiKeyService = generateApiKeyUseCase;
    }

    // ileride Jwt'yi controller içerisinden okuma, bunu bir hata olarak fırlat.
    @PostMapping
    public void generateApiKey(@AuthenticationPrincipal Jwt jwt) {
        String merchantIdClaim = jwt.getClaimAsString("merchant_id");
        if (merchantIdClaim == null || merchantIdClaim.isBlank()) {
            throw new MissingJwtClaimException("merchant_id");
        }

        UUID merchantId = UUID.fromString(merchantIdClaim);

        this.generateApiKeyService.execute(new GenerateApiKeyCommand(merchantId));
    }
}
