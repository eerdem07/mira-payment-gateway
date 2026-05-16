package com.eerdem07.mira.gateway.payments.domain;

public enum RefundStatus {
    CREATED,    // Refund talebi oluşturuldu; acquirer'a henüz gönderilmedi.
    PROCESSING, // Refund isteği alındı; acquirer/POS işleme akışına girdi.
    PENDING,    // Provider refund'ı kabul etti; settlement/banka sonucu bekleniyor.
    SUCCEEDED,  // Refund tamamlandı.
    FAILED,     // Refund başarısız oldu.
    CANCELED    // Refund acquirer'a gitmeden iptal edildi.
}

/*
State transitions:

CREATED    -> PROCESSING, CANCELED
PROCESSING -> PENDING, SUCCEEDED, FAILED, CANCELED
PENDING    -> SUCCEEDED, FAILED
SUCCEEDED  -> terminal
FAILED     -> terminal
CANCELED   -> terminal
 */
