# UC-006: Submit CheckoutSession

## 1. Goal
Customer, hosted checkout page üzerinden kart bilgilerini göndererek ödeme denemesini başlatır.

## 2. Actors
- Customer
- Mira Gateway
- Mock POS

## 3. Preconditions
- CheckoutSession exists.
- CheckoutSession status is OPEN.
- CheckoutSession is not expired.
- PaymentIntent is payable.

## 4. Main Flow
1. Customer clicks Pay on hosted checkout page.
2. Hosted checkout sends submit request to Mira Gateway.
3. Mira validates CheckoutSession token.
4. Mira validates session status and expiration.
5. Mira creates PaymentAttempt.
6. Mira sends authorization request to Mock POS.
7. Mock POS returns approved response.
8. Mira marks PaymentAttempt as SUCCEEDED.
9. Mira marks PaymentIntent as SUCCEEDED.
10. Mira marks CheckoutSession as SUBMITTED.
11. Mira redirects customer to merchant returnUrl.

## 5. Alternative / Failure Flows
- If session is expired, Mira rejects the request.
- If session is canceled, Mira rejects the request.
- If Mock POS declines payment, Mira marks PaymentAttempt as FAILED.
- If Mock POS times out, Mira marks PaymentAttempt according to timeout policy.

## 6. Business Rules
- Only OPEN sessions can be submitted.
- Expired sessions cannot be submitted.
- A submitted session cannot be submitted again.
- Raw card data must not be stored.

## 7. Result
- PaymentAttempt is created.
- PaymentIntent state is updated.
- CheckoutSession state is updated.

## 8. Related APIs
- `POST /v1/checkout-sessions/{token}/submit`

## 9. Related Documents
- `../prd/PRD-001-hosted-checkout-basic-card-payment.md`
- `../api/checkout-sessions.md`