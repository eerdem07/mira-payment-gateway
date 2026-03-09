package com.eerdem07.mira.gateway.payments.domain;

public enum PaymentIntentStatus {
    REQUIRES_PAYMENT_METHOD,
    REQUIRES_CONFIRMATION,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    EXPIRED
}
