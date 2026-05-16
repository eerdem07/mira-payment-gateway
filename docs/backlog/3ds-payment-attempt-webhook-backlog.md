# 3DS Payment Attempt and Webhook Backlog

## Context

Mira Gateway starts the payment attempt and calls Mock Bank POS authorization. If Mock Bank POS decides that 3DS is required, it returns a redirect URL for its own 3DS challenge page.

Mira Gateway does not call `POST /api/v1/pos/3ds/complete`. That endpoint belongs to Mock Bank POS and is called by the Mock Bank POS 3DS challenge website after the customer completes the challenge.

The authoritative payment result reaches Mira Gateway through a Mock Bank POS webhook. Browser redirect or return is only a customer experience signal.

## Target Flow

1. Customer submits card details on hosted checkout.
2. Mira Gateway creates a `PaymentAttempt`.
3. Mira Gateway calls Mock Bank POS authorize.
4. Mock Bank POS returns either an immediate authorization result or a 3DS-required result.
5. If 3DS is required, Mira Gateway stores the attempt as pending 3DS and returns the 3DS redirect action to hosted checkout.
6. Hosted checkout redirects the customer to the Mock Bank POS 3DS challenge page.
7. The Mock Bank POS challenge page calls Mock Bank POS `POST /api/v1/pos/3ds/complete`.
8. Mock Bank POS sends the final 3DS authorization result to Mira Gateway by webhook.
9. Mira Gateway idempotently updates `PaymentAttempt`, `PaymentIntent`, and `CheckoutSession`.
10. Hosted checkout or merchant website displays the final result after polling or status refresh.

## Backlog

### Epic 1: Model 3DS Pending State

#### Story 1.1: Add 3DS-aware payment statuses

Add statuses that represent a payment waiting for customer authentication.

Acceptance criteria:
- `PaymentAttempt` can represent a pending 3DS state.
- `PaymentIntent` can represent a requires-action or processing state while 3DS is not finalized.
- Existing non-3DS card payment behavior remains unchanged.
- State transitions reject invalid moves such as pending 3DS to succeeded without a POS/webhook result.

Suggested names:
- `PaymentAttemptStatus.PENDING_3DS`
- `PaymentIntentStatus.REQUIRES_ACTION` or `PROCESSING`

#### Story 1.2: Store 3DS correlation fields

Persist the identifiers needed to match the authorize response, challenge session, and webhook result.

Acceptance criteria:
- `PaymentAttempt` stores Mock POS transaction id when available.
- `PaymentAttempt` stores `threeDsSessionId` when returned by Mock Bank POS.
- `PaymentAttempt` stores the 3DS redirect URL or next action payload needed by hosted checkout.
- Webhook processing can find the original attempt without using browser session state.

Candidate fields:
- `posTransactionId`
- `posReference`
- `threeDsSessionId`
- `threeDsRedirectUrl`
- `threeDsStatus`
- `eci`

### Epic 2: Extend Mock POS Authorization Contract in Gateway

#### Story 2.1: Support `PENDING_3DS` authorize response

Teach the gateway Mock POS adapter to understand 3DS-required authorization responses.

Acceptance criteria:
- `MockPosAuthorizeResponse` can deserialize `PENDING_3DS`.
- Authorization result can carry 3DS next action data.
- Existing approved, declined, timeout, and format-error paths continue to pass.
- A 3DS-required result does not mark the attempt as succeeded.

Expected Mock Bank POS fields:
- `status = PENDING_3DS`
- `responseCode = PENDING`
- `threeDsSessionId`
- `acsUrl`
- `threeDsFlow`
- `messageVersion`
- `expiresAt`

#### Story 2.2: Add 3DS next action to application result

Expose the redirect action from the application layer to hosted checkout.

Acceptance criteria:
- `PaymentAuthorizationResult` can represent an action required response.
- Result includes redirect URL and session reference.
- Hosted checkout submit response can tell the frontend to redirect.
- No raw card data is stored or returned.

Candidate response shape:

```json
{
  "status": "REQUIRES_ACTION",
  "nextAction": {
    "type": "REDIRECT_3DS",
    "redirectUrl": "http://localhost:5102/3ds/challenge/...",
    "threeDsSessionId": "3ds_..."
  }
}
```

### Epic 3: Hosted Checkout 3DS Redirect UX

#### Story 3.1: Redirect customer to Mock Bank POS challenge

When submit returns a 3DS next action, the hosted checkout page redirects the customer to the Mock Bank POS challenge URL.

Acceptance criteria:
- Hosted checkout detects `REDIRECT_3DS`.
- Customer is redirected to the provided Mock Bank POS URL.
- The redirect URL is not hardcoded in the frontend.
- If the redirect action is missing or expired, hosted checkout shows a recoverable error.

#### Story 3.2: Show waiting state after customer returns

After challenge completion, the customer may return to hosted checkout or merchant website before the webhook is processed.

Acceptance criteria:
- Hosted checkout can show a processing or waiting-for-confirmation screen.
- The UI does not show success until Mira Gateway state is finalized.
- The UI can poll checkout session or payment intent status.
- Timeout messaging is clear if no webhook arrives within the expected window.

### Epic 4: Mock Bank POS Webhook Ingestion

#### Story 4.1: Add Mock Bank POS webhook endpoint

Create a gateway endpoint that receives final 3DS authorization results from Mock Bank POS.

Acceptance criteria:
- Endpoint accepts Mock Bank POS 3DS completion events.
- Endpoint validates provider identity with a shared secret or signature.
- Endpoint responds with 2xx only after the event is safely accepted or idempotently recognized.
- Endpoint does not rely on browser redirect data.

Candidate endpoint:

```http
POST /api/v1/webhooks/mock-bank-pos
```

#### Story 4.2: Define webhook event payload

Define the event contract needed to update payment state.

Acceptance criteria:
- Payload includes a stable event id for idempotency.
- Payload includes original payment attempt correlation data.
- Payload includes final POS authorization fields.
- Payload includes 3DS authentication outcome fields.

Candidate payload:

```json
{
  "eventId": "evt_...",
  "eventType": "mock_pos.3ds.completed",
  "createdAt": "2026-05-14T15:00:00Z",
  "merchantId": "mrc_mock_001",
  "terminalId": "term_mock_001",
  "orderId": "order-123",
  "originalTransactionId": "payment-attempt-id",
  "threeDsSessionId": "3ds_...",
  "status": "AUTHORIZED",
  "approved": true,
  "responseCode": "00",
  "responseMessage": "Approved",
  "posTransactionId": "pos_txn_...",
  "authCode": "A12345",
  "hostReferenceNumber": "HST...",
  "threeDsStatus": "AUTHENTICATED",
  "eci": "05",
  "messageVersion": "2.2.0"
}
```

#### Story 4.3: Process webhook idempotently

Apply webhook events to domain state without duplicate side effects.

Acceptance criteria:
- Same `eventId` can be delivered multiple times without changing state twice.
- Same final POS result can be replayed safely.
- Unknown attempts are recorded or rejected according to the chosen policy.
- Invalid state transitions are rejected and observable.
- Successful 3DS updates attempt and intent to authorized or succeeded according to capture mode.
- Failed, declined, expired, or timeout 3DS updates attempt and intent to failure states.

### Epic 5: Payment Status Query After 3DS

#### Story 5.1: Expose updated checkout/payment status

Hosted checkout and merchant systems need a way to observe the final result after webhook processing.

Acceptance criteria:
- Existing payment intent or checkout session response includes 3DS pending and final states.
- Hosted checkout can poll without exposing sensitive data.
- Merchant can query final payment intent state.
- API response clearly distinguishes `requires_action`, `processing`, `succeeded`, and `failed`.

### Epic 6: Tests and Observability

#### Story 6.1: Add adapter tests for 3DS authorize response

Acceptance criteria:
- Mock POS authorize `PENDING_3DS` response maps to action-required result.
- Existing authorize success and failure tests remain green.
- Missing 3DS fields are handled predictably.

#### Story 6.2: Add service tests for pending 3DS attempt

Acceptance criteria:
- Submit checkout creates attempt.
- 3DS-required authorization stores pending state.
- Payment intent is not marked succeeded before webhook.
- Hosted checkout response includes next action.

#### Story 6.3: Add webhook processing tests

Acceptance criteria:
- Successful 3DS webhook authorizes the payment.
- Failed 3DS webhook fails the attempt.
- Duplicate webhook event is idempotent.
- Webhook for unknown attempt is handled according to policy.
- Invalid signature is rejected.

#### Story 6.4: Add audit logs for 3DS lifecycle

Acceptance criteria:
- Log attempt creation.
- Log 3DS pending transition.
- Log webhook received and processed.
- Log duplicate webhook event.
- Logs do not include PAN, CVC, or raw card data.

## Open Decisions

- Should `PaymentIntent` use `REQUIRES_ACTION` or `PROCESSING` while 3DS is pending?
- Should successful 3DS with `capture=true` mark intent as `SUCCEEDED` immediately or `AUTHORIZED` then capture internally?
- Should webhook endpoint be provider-specific or generic provider-webhook infrastructure?
- What signature scheme should Mock Bank POS use for webhook verification?
- Where should the customer return after Mock Bank POS challenge: hosted checkout status page or merchant return URL?
- What timeout should apply when a 3DS webhook is not received?

## Related Documents

- `../prd/PRD-002-3ds-authentication.md`
- `../use-cases/UC-006: Submit CheckoutSession.md`
- `../mockbankpos/docs/api/mock-pos-3ds-api-contract.md`
- `../api/webhooks.md`
- `../api/payment-attempts.md`
