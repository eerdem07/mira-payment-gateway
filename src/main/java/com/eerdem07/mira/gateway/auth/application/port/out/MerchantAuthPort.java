package com.eerdem07.mira.gateway.auth.application.port.out;

import com.eerdem07.mira.gateway.merchants.domain.Merchant;

import java.util.Optional;

public interface MerchantAuthPort {
    Optional<Merchant> findByEmail(String email);
}

