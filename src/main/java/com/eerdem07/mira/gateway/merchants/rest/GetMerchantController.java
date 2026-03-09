package com.eerdem07.mira.gateway.merchants.rest;

import com.eerdem07.mira.gateway.merchants.application.port.in.GetMerchantQuery;
import com.eerdem07.mira.gateway.merchants.application.port.in.GetMerchantResult;
import com.eerdem07.mira.gateway.merchants.application.port.in.GetMerchantUseCase;
import com.eerdem07.mira.gateway.merchants.rest.dto.GetMerchantResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants")
public class GetMerchantController {
    private final GetMerchantUseCase getMerchantUseCase;

    public GetMerchantController(GetMerchantUseCase getMerchantUseCase) {
        this.getMerchantUseCase = getMerchantUseCase;
    }

    @GetMapping("/{merchantId}")
    public ResponseEntity<GetMerchantResponse> findMerchant(@PathVariable UUID merchantId) {
        GetMerchantResult result = this.getMerchantUseCase.execute(new GetMerchantQuery(merchantId));
        GetMerchantResponse response = new GetMerchantResponse(result.merchantId(), result.legalName(), result.status());
        return ResponseEntity.ok(response);
    }


}
