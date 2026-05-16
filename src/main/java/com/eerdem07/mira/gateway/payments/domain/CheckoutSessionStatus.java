package com.eerdem07.mira.gateway.payments.domain;

public enum CheckoutSessionStatus {
    OPEN,            // Hosted checkout sayfası aktif; müşteri ödeme bilgisi girebilir.
    ACTION_REQUIRED, // 3DS/redirect akışı bekleniyor.
    SUBMITTED,       // Müşteri formu gönderdi; ödeme sonucu PaymentIntent üzerinden takip edilir.
    CANCELED,        // Merchant veya müşteri checkout'u iptal etti.
    EXPIRED          // Checkout session yaşam süresi doldu.
}

/*
State transitions:

OPEN            -> ACTION_REQUIRED, SUBMITTED, CANCELED, EXPIRED
ACTION_REQUIRED -> SUBMITTED, CANCELED, EXPIRED
SUBMITTED       -> terminal
CANCELED        -> terminal
EXPIRED         -> terminal
 */
