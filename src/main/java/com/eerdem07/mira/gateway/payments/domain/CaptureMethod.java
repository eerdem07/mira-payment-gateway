package com.eerdem07.mira.gateway.payments.domain;

public enum CaptureMethod {
    AUTOMATIC, // Authorization ve capture tek authorize isteğinde tamamlanır.
    MANUAL     // Önce authorization/provizyon alınır, capture ayrı çağrıyla yapılır.
}
