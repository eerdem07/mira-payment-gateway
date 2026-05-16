package com.eerdem07.mira.gateway.payments.domain;

public enum ThreeDsAuthenticationStatus {
    INITIATED,           // 3DS authentication başlatıldı.
    CHALLENGE_REQUIRED,  // Müşteri ACS/challenge akışına yönlendirilmeli.
    CHALLENGE_COMPLETED, // Müşteri challenge'ı tamamladı; sonuç işleniyor.
    AUTHENTICATED,       // 3DS doğrulaması başarılı.
    ATTEMPTED,           // 3DS denendi; tam doğrulama olmadı, sonuç alanlarıyla yorumlanır.
    FAILED,              // 3DS başarısız oldu veya teknik hata oluştu.
    EXPIRED              // 3DS authentication süresi doldu.
}

/*
State transitions:

INITIATED           -> CHALLENGE_REQUIRED, AUTHENTICATED, ATTEMPTED, FAILED
CHALLENGE_REQUIRED  -> CHALLENGE_COMPLETED, FAILED, EXPIRED
CHALLENGE_COMPLETED -> AUTHENTICATED, ATTEMPTED, FAILED
AUTHENTICATED       -> terminal
ATTEMPTED           -> terminal
FAILED              -> terminal
EXPIRED             -> terminal
 */
