package com.eerdem07.mira.gateway.merchants.application.port.in;

import com.eerdem07.mira.gateway.merchants.domain.MerchantStatus;

import java.util.UUID;

public record GetMerchantResult(UUID merchantId, String legalName, MerchantStatus status) {
}
