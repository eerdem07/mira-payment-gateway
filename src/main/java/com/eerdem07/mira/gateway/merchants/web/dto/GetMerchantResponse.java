package com.eerdem07.mira.gateway.merchants.web.dto;

import com.eerdem07.mira.gateway.merchants.domain.MerchantStatus;

import java.util.UUID;

public record GetMerchantResponse(UUID merchantId, String legalName, MerchantStatus status) {
}
