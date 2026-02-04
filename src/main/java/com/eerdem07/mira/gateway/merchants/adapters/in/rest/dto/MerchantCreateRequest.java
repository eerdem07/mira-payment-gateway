package com.eerdem07.mira.gateway.merchants.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MerchantCreateRequest(
        @NotBlank
        @Size(min = 2, max = 200)
        String legalName
) {}
