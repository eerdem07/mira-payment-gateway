# Checkout State & Outcome Decisions

Bu dokümanda iki farklı perspektiften bakıyoruz:

1. **State geçişleri** — domain içinde statülerin nasıl değiştiği
2. **Outcome kararları** — POS sonucuna (failureCode / success) göre ne yapılacağı ve bunun downstream etkisi

---

## 1. State Geçişleri (Domain Perspektifi)

Buradaki soru: "Hangi statüden hangi statüye geçilebilir?"

### PaymentIntent

```
REQUIRES_PAYMENT_METHOD ──► PROCESSING ──► SUCCEEDED
                                       ──► REQUIRES_CAPTURE
                                       ──► REQUIRES_ACTION ──► PROCESSING (3DS sonrası)
                                       ──► REQUIRES_PAYMENT_METHOD  (decline → retry mümkün)
                                       ──► FAILED                   (sistem hatası → terminal)

REQUIRES_PAYMENT_METHOD ──► CANCELED   (lost/stolen kart → merchant müdahalesiz terminal)
```

### CheckoutSession

```
OPEN ──► ACTION_REQUIRED (3DS bekleniyor)
     ──► SUBMITTED       (her türlü sonuçta — success, decline, error)
     ──► CANCELED        (kullanıcı veya merchant iptal etti)
     ──► EXPIRED         (süre doldu)
```

**Kritik kural:** CheckoutSession bir kez `SUBMITTED` oldu mu terminaldir. Aynı session üzerinden ikinci submit yapılamaz.

---

## 2. Outcome Kararları (POS Sonucu Perspektifi)

Buradaki soru: "Hangi sonuç geldi, ne yapılacak?"

Karar noktası: POS'tan dönen `failureCode` var mı yok mu, ve varsa hangisi?

### 2.1 failureCode yok → Başarı

| PaymentIntent son durumu | Anlam | Aksiyon |
|---|---|---|
| `SUCCEEDED` | Ödeme tamamlandı | `returnUrl?session_id=...` |
| `REQUIRES_CAPTURE` | Provizyon alındı, capture bekliyor | `returnUrl?session_id=...` |
| `REQUIRES_ACTION` | 3DS gerekiyor | ACS iframe göster |

### 2.2 failureCode var → Başarısızlık

#### Decline grubu — `paymentIntentStatus = REQUIRES_PAYMENT_METHOD`

| failureCode | POS kodu | Intent son durum | Yeni session açılabilir mi? | Frontend |
|---|---|---|---|---|
| `DO_NOT_HONOR` | 05 | `REQUIRES_PAYMENT_METHOD` | ✅ Evet | `cancelUrl?reason=declined&...` |
| `INSUFFICIENT_FUNDS` | 51 | `REQUIRES_PAYMENT_METHOD` | ✅ Evet | `cancelUrl?reason=declined&...` |
| `EXPIRED_CARD` | 54 | `REQUIRES_PAYMENT_METHOD` | ✅ Evet | `cancelUrl?reason=declined&...` |
| `INVALID_CARD_NUMBER` | 14 | `REQUIRES_PAYMENT_METHOD` | ✅ Evet | `cancelUrl?reason=declined&...` |
| `TRANSACTION_NOT_PERMITTED` | 57 | `REQUIRES_PAYMENT_METHOD` | ✅ Evet | `cancelUrl?reason=declined&...` |
| `LIMIT_EXCEEDED` | 61, 65 | `REQUIRES_PAYMENT_METHOD` | ✅ Evet | `cancelUrl?reason=declined&...` |
| `LOST_OR_STOLEN_CARD` | 41, 43 | `CANCELED` ⚠️ | ❌ Hayır | `cancelUrl?reason=fraud&...` |

> **LOST_OR_STOLEN_CARD istisnası:** Domain önce `REQUIRES_PAYMENT_METHOD`'a geçer, ardından `cancel()` çağrılır → intent `CANCELED` olur. `validateCheckoutSessionCreatable()` artık izin vermez. Aynı intent üzerinden hiç kimse yeni ödeme yapamaz.

#### Sistem hatası grubu — `paymentIntentStatus = FAILED`

| failureCode | POS kodu | Intent son durum | Yeni session açılabilir mi? | Frontend |
|---|---|---|---|---|
| `POS_FORMAT_ERROR` | 30, 13 | `FAILED` | ❌ Hayır | `cancelUrl?reason=error&...` |
| `POS_CONFIGURATION_ERROR` | 58 | `FAILED` | ❌ Hayır | `cancelUrl?reason=error&...` |
| `ISSUER_UNAVAILABLE` | 91 | `FAILED` | ❌ Hayır | `cancelUrl?reason=error&...` |
| `POS_SYSTEM_ERROR` | 96 | `FAILED` | ❌ Hayır | `cancelUrl?reason=error&...` |
| `POS_TIMEOUT` | TIMEOUT | `FAILED` | ❌ Hayır | `cancelUrl?reason=error&...` |
| `UNKNOWN_POS_ERROR` | — | `FAILED` | ❌ Hayır | `cancelUrl?reason=error&...` |

---

## 3. İki Perspektifin Farkı

| | State geçişleri | Outcome kararları |
|---|---|---|
| **Soru** | Hangi statüden hangi statüye geçilebilir? | Bu sonuç geldi, ne yapılacak? |
| **Sahip** | Domain katmanı (`PaymentIntent`, `CheckoutSession`) | Application katmanı (`SubmitCheckoutSessionService`) |
| **Girdi** | Mevcut statü | POS sonucu (`failureCode`, `status`) |
| **Çıktı** | Yeni statü | Statü değişimi + downstream aksiyon |
| **Örnek** | `PROCESSING → FAILED` geçişi geçerli mi? | `POS_TIMEOUT` geldi → intent `FAILED` yap, merchant'a retry sunma |

**LOST_OR_STOLEN_CARD** bu iki perspektifin kesiştiği en net örnektir:
- State geçişi olarak bakınca: `REQUIRES_PAYMENT_METHOD → CANCELED` geçerli bir domain işlemi
- Outcome kararı olarak bakınca: fraud sinyali geldi → domain'i bu geçişe zorla, yeni session kapısını kapat

---

## 4. Merchant Tarafı Retry Kuralı

```
paymentIntentStatus == REQUIRES_PAYMENT_METHOD
  AND failureCode != LOST_OR_STOLEN_CARD
    → merchant yeni CheckoutSession açabilir (aynı intent üzerinden)

paymentIntentStatus == FAILED
OR paymentIntentStatus == CANCELED
    → yeni CheckoutSession açılamaz
```

Yeni CheckoutSession yaratma: `POST /v1/payment-intents/{id}/checkout-sessions` (API key gerekli).  
Hosted page bu endpoint'i çağıramaz — retry akışı merchant backend üzerinden yürür.

---

## 5. cancelUrl Query Parametreleri

Merchant'ın decline / error / fraud ayrımını yapabilmesi için:

```
cancelUrl?reason=declined&session_id={id}&failure_code={code}   → retry sunulabilir
cancelUrl?reason=fraud&session_id={id}&failure_code=LOST_OR_STOLEN_CARD  → retry sunulmamalı
cancelUrl?reason=error&session_id={id}&failure_code={code}      → retry sunulamaz (FAILED intent)
cancelUrl?reason=canceled&session_id={id}                       → kullanıcı iptal etti
```
