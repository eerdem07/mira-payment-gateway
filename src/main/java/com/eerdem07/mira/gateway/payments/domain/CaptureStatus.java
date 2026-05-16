package com.eerdem07.mira.gateway.payments.domain;

public enum CaptureStatus {
    CREATED,    // Capture talebi oluşturuldu; acquirer'a henüz gönderilmedi.
    PROCESSING, // Capture isteği acquirer/POS tarafından işleniyor.
    SUCCEEDED,  // Capture başarılı; para çekildi.
    FAILED,     // Capture başarısız oldu.
    CANCELED    // Capture talebi acquirer'a gitmeden iptal edildi.
}

/*
State transitions:

CREATED    -> PROCESSING, CANCELED
PROCESSING -> SUCCEEDED, FAILED
SUCCEEDED  -> terminal
FAILED     -> terminal
CANCELED   -> terminal

Retry rule:

FAILED capture records are not reused. A retry creates a new Capture object.
 */
