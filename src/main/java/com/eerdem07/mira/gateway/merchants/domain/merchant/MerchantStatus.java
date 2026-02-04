package com.eerdem07.mira.gateway.merchants.domain.merchant;

public enum MerchantStatus {
    PENDING,
    ACTIVE,
    SUSPENDED;

    public boolean canActivate() {
        return this == PENDING;
    }

    public boolean canSuspend() {
        return this == ACTIVE;
    }
}
