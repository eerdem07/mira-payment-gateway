package com.eerdem07.mira.gateway.payments.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

public class PaymentIntent {
    private String id;
    private String merchantId;
    private BigDecimal amount;
    private Currency currency;
    private String merchantReference;
    // merchantReference, merchant tarafındaki müşterinin sipariş id'sini tutyor.
    private String description;
    private PaymentIntentStatus status;
    private int attemptCount;
    private String failureCode;
    private String failureMessage;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    private Instant canceledAt;

    // domain behaviors:
    // attachPaymentMethod(...)
    // markRequiresConfirmation()
    // markProcessing()
    // markSucceeded()
    // markFailed(...)
    // cancel()
    // expire()
    // validateConfirmable()
}


// bir adet Money ValueObject kurulacak. şimdilik amount ve currency kullandık.
//