package com.eerdem07.mira.gateway.merchants.adapters.in.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record MerchantCreateResponse (UUID merchantId,
                                      String legalName,
                                      String status,
                                      Instant createdAt,
                                      Instant activatedAt,
                                      Instant suspendedAt){
}
