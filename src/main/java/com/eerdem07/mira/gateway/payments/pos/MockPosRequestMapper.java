package com.eerdem07.mira.gateway.payments.pos;

import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAuthorizationRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentCaptureRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentRefundRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentVoidRequest;
import com.eerdem07.mira.gateway.payments.application.port.out.ThreeDsCompleteRequest;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPos3DsCompleteRequest;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPosAuthorizeRequest;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPosCaptureRequest;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPosCardRequest;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPosRefundRequest;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPosVoidRequest;

import java.math.BigDecimal;

final class MockPosRequestMapper {

    private final String merchantId;
    private final String terminalId;

    MockPosRequestMapper(String merchantId, String terminalId) {
        this.merchantId = merchantId;
        this.terminalId = terminalId;
    }

    MockPosAuthorizeRequest toAuthorizeRequest(PaymentAuthorizationRequest request) {
        return new MockPosAuthorizeRequest(
                merchantId,
                terminalId,
                request.orderId(),
                request.paymentAttemptId().toString(),
                formatAmount(request.amount()),
                request.currency(),
                request.installmentCount(),
                request.capture(),
                new MockPosCardRequest(
                        request.cardHolderName(),
                        request.cardNumber(),
                        request.expiryMonth(),
                        request.expiryYear(),
                        request.cvc()
                )
        );
    }

    MockPosCaptureRequest toCaptureRequest(PaymentCaptureRequest request) {
        return new MockPosCaptureRequest(
                merchantId,
                terminalId,
                request.orderId(),
                request.captureId().toString(),
                request.originalPaymentAttemptId().toString(),
                request.originalPosTransactionId(),
                request.authorizationCode(),
                request.posReference(),
                formatAmount(request.amount()),
                request.currency()
        );
    }

    MockPosVoidRequest toVoidRequest(PaymentVoidRequest request) {
        return new MockPosVoidRequest(
                merchantId,
                terminalId,
                request.orderId(),
                request.voidId().toString(),
                request.originalPaymentAttemptId().toString(),
                request.originalPosTransactionId(),
                request.authorizationCode(),
                request.posReference(),
                formatAmount(request.amount()),
                request.currency()
        );
    }

    MockPosRefundRequest toRefundRequest(PaymentRefundRequest request) {
        return new MockPosRefundRequest(
                merchantId,
                terminalId,
                request.orderId(),
                request.refundId().toString(),
                request.originalPaymentAttemptId().toString(),
                request.originalPosTransactionId(),
                request.authorizationCode(),
                request.posReference(),
                formatAmount(request.amount()),
                request.currency()
        );
    }

    MockPos3DsCompleteRequest to3DsCompleteRequest(ThreeDsCompleteRequest request) {
        return new MockPos3DsCompleteRequest(
                merchantId,
                terminalId,
                request.orderId(),
                request.paymentAttemptId().toString() + "_3ds_complete",
                request.threeDsSessionId()
        );
    }

    private String formatAmount(BigDecimal amount) {
        return amount.toPlainString();
    }
}
