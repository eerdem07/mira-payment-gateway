package com.eerdem07.mira.gateway.payments.domain;

public enum PaymentAttemptStatus {
    INITIATED,       // Attempt oluşturuldu; acquirer/POS isteği henüz tamamlanmadı.
    PROCESSING,      // Acquirer/POS authorize isteği işleniyor.
    REQUIRES_ACTION, // Bu attempt için 3DS/SCA aksiyonu gerekiyor.
    AUTHORIZED,      // Manual capture için provizyon başarılı.
    SUCCEEDED,       // Ödeme tamamlandı; automatic capture veya manual capture başarılı.
    REFUNDED,        // Tamamlanmış ödeme iade edildi.
    DECLINED,        // Issuer/acquirer ödemeyi reddetti.
    FAILED,          // Teknik hata veya provider problemi oluştu.
    CANCELED,        // Attempt acquirer'a gitmeden iptal edildi.
    VOIDED,          // Authorized attempt capture edilmeden iptal edildi.
    EXPIRED          // Attempt veya authorization window süresi doldu.
}

/*
State transitions:

INITIATED       -> PROCESSING, CANCELED
PROCESSING      -> REQUIRES_ACTION, AUTHORIZED, SUCCEEDED, DECLINED, FAILED
REQUIRES_ACTION -> PROCESSING, DECLINED, FAILED, CANCELED, EXPIRED
AUTHORIZED      -> SUCCEEDED, VOIDED, EXPIRED
SUCCEEDED       -> REFUNDED
REFUNDED        -> terminal
DECLINED        -> terminal
FAILED          -> terminal
CANCELED        -> terminal
VOIDED          -> terminal
EXPIRED         -> terminal
 */
