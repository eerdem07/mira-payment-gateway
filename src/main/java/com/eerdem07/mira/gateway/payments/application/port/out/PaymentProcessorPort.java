package com.eerdem07.mira.gateway.payments.application.port.out;

public interface PaymentProcessorPort {
    PaymentAuthorizationResult authorize(PaymentAuthorizationRequest request);

    PaymentAuthorizationResult complete3ds(ThreeDsCompleteRequest request);

    PaymentCaptureResult capture(PaymentCaptureRequest request);

    PaymentVoidResult voidAuthorization(PaymentVoidRequest request);

    PaymentRefundResult refund(PaymentRefundRequest request);
}
