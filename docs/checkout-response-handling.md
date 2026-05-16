# Checkout Session — Response Handling Reference

Bu döküman `POST /submit` ve `POST /3ds/complete` endpoint'lerinin dönebileceği **tüm olası durumları** listeler. Frontend bu tablolara göre UI state'ini ayarlamalıdır.

---

## POST `/v1/checkout-sessions/{token}/submit` — Olası Sonuçlar

Her senaryoda `checkoutSessionStatus` ve `paymentIntentStatus` birlikte değerlendirilmelidir.

### Senaryo 1 — Ödeme Başarılı (Automatic Capture)

```json
{
  "checkoutSessionStatus": "SUBMITTED",
  "paymentIntentStatus": "SUCCEEDED",
  "returnUrl": "https://merchant.com/success",
  "failureCode": null,
  "failureMessage": null,
  "acsUrl": null,
  "threeDsFlow": null
}
```

**Aksiyon:** `returnUrl?session_id={checkoutSessionId}` adresine yönlendir.

---

### Senaryo 2 — Provizyon Alındı (Manual Capture)

```json
{
  "checkoutSessionStatus": "SUBMITTED",
  "paymentIntentStatus": "REQUIRES_CAPTURE",
  "returnUrl": "https://merchant.com/success",
  "failureCode": null,
  "failureMessage": null,
  "acsUrl": null,
  "threeDsFlow": null
}
```

**Aksiyon:** Müşteri açısından ödeme tamamlandı. `returnUrl?session_id={checkoutSessionId}` adresine yönlendir.  
`REQUIRES_CAPTURE` merchant tarafının ayrıca capture işlemi yapması gereken bir backend durumudur; müşteri bunu görmez.

---

### Senaryo 3 — Kart Reddedildi (Decline)

```json
{
  "checkoutSessionStatus": "SUBMITTED",
  "paymentIntentStatus": "REQUIRES_PAYMENT_METHOD",
  "returnUrl": "https://merchant.com/success",
  "failureCode": "INSUFFICIENT_FUNDS",
  "failureMessage": "Insufficient funds",
  "acsUrl": null,
  "threeDsFlow": null
}
```

**Aksiyon:** Hata ekranı göster, `failureCode` + `failureMessage` yaz.  
**ÖNEMLİ:** `checkoutSessionStatus === "SUBMITTED"` olduğundan tekrar deneme (`retry`) yapılamaz. Kullanıcıya "ödeme reddedildi" sonuç ekranı göster, yeni ödeme için merchant'ın yeniden bir session başlatması gerekir.

---

### Senaryo 4 — Sistem Hatası

```json
{
  "checkoutSessionStatus": "SUBMITTED",
  "paymentIntentStatus": "FAILED",
  "returnUrl": "https://merchant.com/success",
  "failureCode": "UNKNOWN_POS_ERROR",
  "failureMessage": "Payment authorization failed",
  "acsUrl": null,
  "threeDsFlow": null
}
```

**Aksiyon:** Hata ekranı göster. Bu bir POS/ağ seviyesindeki teknik hata — kart reddinden farklı.

---

### Senaryo 5 — 3DS Gerekiyor (Frictionless veya Challenge)

```json
{
  "checkoutSessionStatus": "ACTION_REQUIRED",
  "paymentIntentStatus": "REQUIRES_ACTION",
  "returnUrl": "https://merchant.com/success",
  "failureCode": null,
  "failureMessage": null,
  "acsUrl": "http://localhost:5102/3ds/acs?session=...",
  "threeDsFlow": "CHALLENGE"
}
```

`threeDsFlow` değerleri: `"FRICTIONLESS"` veya `"CHALLENGE"`

**Aksiyon:** `acsUrl`'i iframe içinde göster. Kullanıcı 3DS'i tamamlayınca `POST /3ds/complete` çağır.

---

## POST `/v1/checkout-sessions/{token}/3ds/complete` — Olası Sonuçlar

### Senaryo 1 — 3DS Sonrası Ödeme Başarılı

```json
{
  "checkoutSessionStatus": "SUBMITTED",
  "paymentIntentStatus": "SUCCEEDED",
  "returnUrl": "https://merchant.com/success",
  "failureCode": null,
  "failureMessage": null
}
```

**Aksiyon:** `returnUrl?session_id={checkoutSessionId}` adresine yönlendir.

---

### Senaryo 2 — 3DS Sonrası Provizyon Alındı (Manual Capture)

```json
{
  "checkoutSessionStatus": "SUBMITTED",
  "paymentIntentStatus": "REQUIRES_CAPTURE",
  "returnUrl": "https://merchant.com/success",
  "failureCode": null,
  "failureMessage": null
}
```

**Aksiyon:** `returnUrl?session_id={checkoutSessionId}` adresine yönlendir. (Submit Senaryo 2 ile aynı kural.)

---

### Senaryo 3 — 3DS Sonrası Kart Reddedildi

```json
{
  "checkoutSessionStatus": "SUBMITTED",
  "paymentIntentStatus": "REQUIRES_PAYMENT_METHOD",
  "returnUrl": "https://merchant.com/success",
  "failureCode": "DO_NOT_HONOR",
  "failureMessage": "Transaction declined by issuer"
}
```

**Aksiyon:** Hata ekranı göster, retry yok.

---

### Senaryo 4 — 3DS Sonrası Sistem Hatası

```json
{
  "checkoutSessionStatus": "SUBMITTED",
  "paymentIntentStatus": "FAILED",
  "returnUrl": "https://merchant.com/success",
  "failureCode": "UNKNOWN_POS_ERROR",
  "failureMessage": "3DS authentication or authorization failed"
}
```

**Aksiyon:** Hata ekranı göster, retry yok.

---

## Karar Ağacı — Her İki Endpoint İçin

```
paymentIntentStatus?
├── SUCCEEDED           → redirect returnUrl
├── REQUIRES_CAPTURE    → redirect returnUrl  (müşteri için "başarılı")
├── REQUIRES_ACTION     → 3DS iframe göster (sadece /submit'ten gelebilir)
├── REQUIRES_PAYMENT_METHOD → hata ekranı, failureCode/failureMessage göster, retry yok
└── FAILED              → hata ekranı, failureCode/failureMessage göster, retry yok
```

---

## Kritik Notlar

1. **Retry yoktur.** `checkoutSessionStatus === "SUBMITTED"` olduğu anda session terminaldir. Başarısız bir ödeme sonrasında kullanıcıya "tekrar dene" butonu gösterme — merchant yeni bir CheckoutSession oluşturmalıdır.

2. **`REQUIRES_CAPTURE` ≠ failure.** Bu status, ödemenin başarıyla provize edildiğini, ancak merchant'ın henüz capture yapmadığını gösterir. Müşteri redirect yapılmalı.

3. **3DS sadece `/submit` dönüşünde tetiklenir.** `/3ds/complete` asla `REQUIRES_ACTION` dönmez.

4. **`failureCode` ve `failureMessage`:** `failureCode` makine-okunabilir (örn. `INSUFFICIENT_FUNDS`, `DO_NOT_HONOR`, `UNKNOWN_POS_ERROR`), `failureMessage` insan-okunabilir. UI'da her ikisini de kullan.

5. **Yönlendirme formatı:** `{returnUrl}?session_id={checkoutSessionId}` — `window.location.href` kullan, React Router değil.
