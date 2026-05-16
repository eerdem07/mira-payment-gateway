# Hosted Checkout Page — Agent Prompt

You are building the **hosted checkout page** for Mira Payment Gateway. This is a React single-page app served at `pay.mira.com/{token}`. The backend is a Spring Boot REST API.

Your job is to implement the full checkout UI: load session data on init, render the card form, submit payment, handle 3DS authentication, and redirect on completion.

---

## Base URL

All API calls go to `http://localhost:8080` in development (or the value of `VITE_API_BASE_URL` env var).

All checkout endpoints are **public** — no authentication headers required.

---

## Endpoints

### 1. GET session details (call on page init)

```
GET /v1/checkout-sessions/{token}
```

**Response 200:**
```json
{
  "id": "uuid",
  "status": "OPEN",
  "expiresAt": "2026-05-16T15:00:00Z",
  "returnUrl": "https://merchant.com/success",
  "cancelUrl": "https://merchant.com/cancel",
  "payment": {
    "amount": 150.00,
    "currency": "TRY",
    "description": "Order #1234"
  }
}
```

**Possible `status` values:** `OPEN`, `ACTION_REQUIRED`, `SUBMITTED`, `CANCELED`, `EXPIRED`

**Error cases:**
- `404` → session not found → show "Geçersiz ödeme bağlantısı" error page
- `status === SUBMITTED` → show "Bu ödeme zaten tamamlandı" page
- `status === CANCELED` → show "Bu ödeme iptal edildi" page
- `status === EXPIRED` → show "Bu ödeme bağlantısının süresi doldu" page

---

### 2. POST submit checkout (call on form submit)

```
POST /v1/checkout-sessions/{token}/submit
Content-Type: application/json
```

**Request body:**
```json
{
  "cardNumber": "4111111111111111",
  "expiryMonth": "12",
  "expiryYear": "2028",
  "cvc": "123",
  "cardHolderName": "John Doe"
}
```

**Validation rules (enforce client-side before sending):**
- `cardNumber`: digits only, 12–19 chars
- `expiryMonth`: `"01"` through `"12"` (always 2-digit string)
- `expiryYear`: 4-digit string
- `cvc`: 3–4 digits
- `cardHolderName`: required, max 200 chars

**Response 200:**
```json
{
  "checkoutSessionId": "uuid",
  "paymentIntentId": "uuid",
  "checkoutSessionStatus": "SUBMITTED",
  "paymentIntentStatus": "SUCCEEDED",
  "returnUrl": "https://merchant.com/success",
  "failureCode": null,
  "failureMessage": null,
  "acsUrl": null,
  "threeDsFlow": null
}
```

**Handle response by `paymentIntentStatus`:**

| `paymentIntentStatus` | Action |
|---|---|
| `SUCCEEDED` | Redirect to `returnUrl` (append `?session_id={checkoutSessionId}`) |
| `REQUIRES_ACTION` | Show 3DS iframe using `acsUrl`. `threeDsFlow` will be `"FRICTIONLESS"` or `"CHALLENGE"` |
| `FAILED` | Show failure screen with `failureCode` + `failureMessage` |

**Error cases:**
- `422` → domain error (already submitted, expired, etc.) → parse `message` field and show to user
- `400` → validation error → show field-level errors from the `errors` array if present

---

### 3. POST complete 3DS (call after ACS redirect/callback)

```
POST /v1/checkout-sessions/{token}/3ds/complete
```

No request body.

**Response 200:**
```json
{
  "checkoutSessionId": "uuid",
  "paymentIntentId": "uuid",
  "checkoutSessionStatus": "SUBMITTED",
  "paymentIntentStatus": "SUCCEEDED",
  "returnUrl": "https://merchant.com/success",
  "failureCode": null,
  "failureMessage": null
}
```

**Handle response by `paymentIntentStatus`:**

| `paymentIntentStatus` | Action |
|---|---|
| `SUCCEEDED` | Redirect to `returnUrl` (append `?session_id={checkoutSessionId}`) |
| `FAILED` | Show failure screen with `failureCode` + `failureMessage` |

---

## Page Flow (State Machine)

```
URL: /checkout/{token}
         │
         ▼
    [LOADING] ──── GET /v1/checkout-sessions/{token}
         │
    ┌────┴─────────────────────────────────┐
    │ status check                         │
    ▼                                      ▼
[ERROR_PAGE]                         [CARD_FORM]
(SUBMITTED/CANCELED/EXPIRED/404)     (OPEN or ACTION_REQUIRED)
                                           │
                                     user submits form
                                           │
                                           ▼
                                     [SUBMITTING] ── POST .../submit
                                           │
                              ┌────────────┼──────────────┐
                              ▼            ▼              ▼
                         [SUCCESS]    [3DS_IFRAME]   [PAYMENT_FAILED]
                         redirect     show acsUrl    show failureCode
                                           │
                                  user completes 3DS
                                           │
                                           ▼
                                   [COMPLETING_3DS] ── POST .../3ds/complete
                                           │
                                  ┌────────┴──────────┐
                                  ▼                   ▼
                             [SUCCESS]         [PAYMENT_FAILED]
                             redirect          show failureCode
```

---

## UI Requirements

### Card Form Fields
- **Card number**: masked input (groups of 4 digits: `4111 1111 1111 1111`)
- **Expiry**: `MM/YY` or `MM/YYYY` format (split to `expiryMonth` / `expiryYear` before sending)
- **CVC**: max 4 digits, masked
- **Cardholder name**: plain text input

### Order Summary (always visible)
- Merchant description: `payment.description`
- Amount + currency: `payment.amount` formatted with `payment.currency` (e.g., `₺150,00`)
- Session expiry countdown (show warning if < 5 minutes left)

### 3DS Handling
- When `paymentIntentStatus === REQUIRES_ACTION`, show the `acsUrl` in a full-screen iframe overlay
- After the iframe signals completion (via `postMessage` from the ACS page or on a manual "I completed authentication" button), call `POST .../3ds/complete`
- Do not rely solely on postMessage since mock ACS may not send it; always provide a "I completed authentication" button

### Loading & Error States
- Show a spinner during API calls
- Show inline field validation errors before submission
- For 422/domain errors, show a dismissible banner at the top of the form with the error message
- For network errors, show "Bağlantı hatası, lütfen tekrar deneyin" with a retry button

### Expiry Countdown
- On `LOADING` success, compute `expiresAt - now` in seconds
- Display a countdown timer in the header
- When timer reaches 0, transition to the `EXPIRED` error page (do not allow submission)

---

## Token Extraction

Extract the token from the URL path:

```ts
// React Router v6
const { token } = useParams<{ token: string }>();
```

The token is an opaque string (UUID-like). Pass it as-is to all API calls.

---

## Environment Variable

```
VITE_API_BASE_URL=http://localhost:8080
```

---

## Success Redirect

Always redirect to:
```
{returnUrl}?session_id={checkoutSessionId}
```

Use `window.location.href = ...` (not React Router) since `returnUrl` is an external merchant URL.

---

## What NOT to do

- Do not store card data in state beyond the form submit call
- Do not log card numbers or CVCs to console
- Do not retry the submit endpoint automatically on failure — let the user retry manually
- Do not call `3ds/complete` unless `paymentIntentStatus === REQUIRES_ACTION` was returned from submit
