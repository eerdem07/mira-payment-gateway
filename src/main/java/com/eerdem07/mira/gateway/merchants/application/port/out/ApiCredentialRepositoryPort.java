package com.eerdem07.mira.gateway.merchants.application.port.out;

import com.eerdem07.mira.gateway.merchants.domain.ApiCredential;

public interface ApiCredentialRepositoryPort {
    void save(ApiCredential apiCredential);
}
