package com.eerdem07.mira.gateway.merchants.adapters.in.rest;

import com.eerdem07.mira.gateway.merchants.adapters.in.rest.dto.MerchantCreateRequest;
import com.eerdem07.mira.gateway.merchants.adapters.in.rest.dto.MerchantCreateResponse;
import com.eerdem07.mira.gateway.merchants.application.port.in.ActivateMerchantUseCase;
import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantCommand;
import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;


@RestController
@RequestMapping("/v1/merchants")
public class MerchantController {
    private final RegisterMerchantUseCase registerMerchant;
    private final ActivateMerchantUseCase activeMerchant;

    public MerchantController(RegisterMerchantUseCase registerMerchant, ActivateMerchantUseCase activeMerchant){
        this.registerMerchant = registerMerchant;
        this.activeMerchant = activeMerchant;
    }

    @PostMapping
    public ResponseEntity<MerchantCreateResponse> register(@Valid @RequestBody MerchantCreateRequest request){
        var result = registerMerchant.register(new RegisterMerchantCommand(request.legalName()));

        var body = new MerchantCreateResponse(result.merchantId(), request.legalName(), "PENDING", Instant.now(), null, null);

        return ResponseEntity.created(URI.create("/v1/merchants/" + result.merchantId())).body(body);
    }

    @PostMapping("/{merchantId}/activate")
    public ResponseEntity<Void> activate(@PathVariable UUID merchantId){
        activeMerchant.activate(merchantId);

        return ResponseEntity.noContent().build();
    }

}


