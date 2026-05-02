package com.eerdem07.mira.gateway.merchants.rest.dto;

public record GenerateApiKeyMerchantResponse (String keyId, String plainSecret, String basicAuthToken){
}
