package com.eerdem07.mira.gateway.merchants.rest;

import com.eerdem07.mira.gateway.merchants.application.port.in.ActivateMerchantUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/")
public class ActivateMerchantController {
    private final ActivateMerchantUseCase activeMerchant;

    public ActivateMerchantController(ActivateMerchantUseCase activeMerchant) {
        this.activeMerchant = activeMerchant;
    }

    @PostMapping("/{merchantId}/activate")
    public ResponseEntity<Void> activate(@PathVariable UUID merchantId) {
        activeMerchant.execute(merchantId);

        return ResponseEntity.noContent()
                .build();
    }
}
