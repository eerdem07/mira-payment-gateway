package com.eerdem07.mira.gateway.payments.domain.model.PaymentIntent;

public class PaymentIntent {
    private long paymentIntentId;
    private long merchantId;
    private long money;
    private String currency;
    private String captureMethod; // AUTOMATIC-MANUAL
    private String status;
    private String paymentMethodRef;
}
