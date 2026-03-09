package com.eerdem07.mira.gateway.payments.domain;

import java.time.Instant;

public class CheckoutSession {
    private String id;
    private String paymentIntentId;
    private String token;
    private CheckoutSessionStatus status;
    private String returnUrl;
    private String cancelUrl;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    private Instant canceledAt;

    // domain behaviors:
    // validateAccessible()
    // validateOpen()
    // complete()
    // cancel()
    // expire()
    // isExpired()


    // https://pay.mira.com/checkout/{token}
}
