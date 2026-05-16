package com.eerdem07.mira.gateway.payments.domain;

public enum PaymentIntentStatus {
    REQUIRES_PAYMENT_METHOD, // Intent oluşturuldu; ödeme yöntemi/kart bekleniyor.
    REQUIRES_ACTION,         // 3DS/SCA gibi müşteri aksiyonu gerekiyor.
    PROCESSING,              // Ödeme veya authorization sonucu işleniyor.
    REQUIRES_CAPTURE,        // Manual capture akışında provizyon alındı, capture bekleniyor.
    SUCCEEDED,               // Para çekildi; ödeme tamamlandı.
    REFUNDED,                // Ödeme tamamen iade edildi.
    FAILED,                  // Kalıcı başarısızlık oluştu; retry yapılmayacak.
    CANCELED,                // Merchant/customer iptal etti veya authorization void edildi.
    EXPIRED                  // Intent veya authorization window süresi doldu.
}

/*
State transitions:

REQUIRES_PAYMENT_METHOD -> PROCESSING, CANCELED, EXPIRED
PROCESSING              -> REQUIRES_ACTION, REQUIRES_CAPTURE, SUCCEEDED, REQUIRES_PAYMENT_METHOD, FAILED
REQUIRES_ACTION         -> PROCESSING, REQUIRES_PAYMENT_METHOD, FAILED, CANCELED, EXPIRED
REQUIRES_CAPTURE        -> SUCCEEDED, CANCELED, EXPIRED
SUCCEEDED               -> REFUNDED
REFUNDED                -> terminal
FAILED                  -> terminal
CANCELED                -> terminal
EXPIRED                 -> terminal
 */
