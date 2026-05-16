package com.eerdem07.mira.gateway.payments.pos;

import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAuthorizationResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentAuthorizationStatus;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentCaptureResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentCaptureStatus;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentRefundResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentRefundStatus;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentVoidResult;
import com.eerdem07.mira.gateway.payments.application.port.out.PaymentVoidStatus;
import com.eerdem07.mira.gateway.payments.domain.PaymentFailureCode;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPos3DsCompleteResponse;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPosAuthorizeResponse;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPosCaptureResponse;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPosRefundResponse;
import com.eerdem07.mira.gateway.payments.pos.dto.MockPosVoidResponse;

final class MockPosResultMapper {

    private final String posProvider;

    MockPosResultMapper(String posProvider) {
        this.posProvider = posProvider;
    }

    PaymentAuthorizationResult toAuthorizationResult(MockPosAuthorizeResponse response) {
        if (response == null) {
            return authorizationError("96", "System malfunction");
        }

        PaymentAuthorizationStatus status = normalizeAuthorizationStatus(response.status());
        String responseCode = defaultIfBlank(response.responseCode(), "96");
        String responseMessage = defaultIfBlank(response.responseMessage(), "System malfunction");

        if (status == PaymentAuthorizationStatus.PENDING) {
            return new PaymentAuthorizationResult(
                    status,
                    posProvider,
                    null,
                    null,
                    null,
                    responseCode,
                    responseMessage,
                    null,
                    null,
                    response.threeDsSessionId(),
                    response.acsUrl(),
                    response.threeDsFlow()
            );
        }

        if (status == PaymentAuthorizationStatus.AUTHORIZED) {
            return new PaymentAuthorizationResult(
                    status,
                    posProvider,
                    response.posTransactionId(),
                    response.authCode(),
                    response.hostReferenceNumber(),
                    responseCode,
                    responseMessage,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        return new PaymentAuthorizationResult(
                status,
                posProvider,
                response.posTransactionId(),
                response.authCode(),
                response.hostReferenceNumber(),
                responseCode,
                responseMessage,
                normalizeAuthorizationFailureCode(status, responseCode),
                responseMessage,
                null,
                null,
                null
        );
    }

    PaymentAuthorizationResult to3DsCompleteResult(MockPos3DsCompleteResponse response) {
        if (response == null) {
            return authorizationError("96", "System malfunction");
        }

        PaymentAuthorizationStatus status = normalizeAuthorizationStatus(response.status());
        String responseCode = defaultIfBlank(response.responseCode(), "96");
        String responseMessage = defaultIfBlank(response.responseMessage(), "System malfunction");

        if (status == PaymentAuthorizationStatus.AUTHORIZED) {
            return new PaymentAuthorizationResult(
                    status,
                    posProvider,
                    response.posTransactionId(),
                    response.authCode(),
                    response.hostReferenceNumber(),
                    responseCode,
                    responseMessage,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        return new PaymentAuthorizationResult(
                status,
                posProvider,
                response.posTransactionId(),
                response.authCode(),
                response.hostReferenceNumber(),
                responseCode,
                responseMessage,
                normalizeAuthorizationFailureCode(status, responseCode),
                responseMessage,
                null,
                null,
                null
        );
    }

    PaymentCaptureResult toCaptureResult(MockPosCaptureResponse response) {
        if (response == null) {
            return captureError("96", "System malfunction");
        }

        PaymentCaptureStatus status = normalizeCaptureStatus(response.status(), response.approved());
        String responseCode = defaultIfBlank(response.responseCode(), "96");
        String responseMessage = defaultIfBlank(response.responseMessage(), "System malfunction");

        if (status == PaymentCaptureStatus.SUCCEEDED) {
            return new PaymentCaptureResult(
                    status,
                    posProvider,
                    response.posCaptureId(),
                    response.hostReferenceNumber(),
                    responseCode,
                    responseMessage,
                    null,
                    null
            );
        }

        return new PaymentCaptureResult(
                status,
                posProvider,
                response.posCaptureId(),
                response.hostReferenceNumber(),
                responseCode,
                responseMessage,
                normalizePosOperationFailureCode(responseCode),
                responseMessage
        );
    }

    PaymentVoidResult toVoidResult(MockPosVoidResponse response) {
        if (response == null) {
            return voidError("96", "System malfunction");
        }

        PaymentVoidStatus status = normalizeVoidStatus(response.status(), response.approved());
        String responseCode = defaultIfBlank(response.responseCode(), "96");
        String responseMessage = defaultIfBlank(response.responseMessage(), "System malfunction");

        if (status == PaymentVoidStatus.SUCCEEDED) {
            return new PaymentVoidResult(
                    status,
                    posProvider,
                    response.posVoidId(),
                    response.hostReferenceNumber(),
                    responseCode,
                    responseMessage,
                    null,
                    null
            );
        }

        return new PaymentVoidResult(
                status,
                posProvider,
                response.posVoidId(),
                response.hostReferenceNumber(),
                responseCode,
                responseMessage,
                normalizePosOperationFailureCode(responseCode),
                responseMessage
        );
    }

    PaymentRefundResult toRefundResult(MockPosRefundResponse response) {
        if (response == null) {
            return refundError("96", "System malfunction");
        }

        PaymentRefundStatus status = normalizeRefundStatus(response.status(), response.approved());
        String responseCode = defaultIfBlank(response.responseCode(), "96");
        String responseMessage = defaultIfBlank(response.responseMessage(), "System malfunction");

        if (status == PaymentRefundStatus.SUCCEEDED) {
            return new PaymentRefundResult(
                    status,
                    posProvider,
                    response.posRefundId(),
                    response.hostReferenceNumber(),
                    responseCode,
                    responseMessage,
                    null,
                    null
            );
        }

        return new PaymentRefundResult(
                status,
                posProvider,
                response.posRefundId(),
                response.hostReferenceNumber(),
                responseCode,
                responseMessage,
                normalizePosOperationFailureCode(responseCode),
                responseMessage
        );
    }

    PaymentAuthorizationResult authorizationError(String responseCode, String responseMessage) {
        return new PaymentAuthorizationResult(
                PaymentAuthorizationStatus.ERROR,
                posProvider,
                null,
                null,
                null,
                responseCode,
                responseMessage,
                normalizeAuthorizationFailureCode(PaymentAuthorizationStatus.ERROR, responseCode),
                responseMessage,
                null,
                null,
                null
        );
    }

    PaymentCaptureResult captureError(String responseCode, String responseMessage) {
        return new PaymentCaptureResult(
                PaymentCaptureStatus.ERROR,
                posProvider,
                null,
                null,
                responseCode,
                responseMessage,
                normalizePosOperationFailureCode(responseCode),
                responseMessage
        );
    }

    PaymentVoidResult voidError(String responseCode, String responseMessage) {
        return new PaymentVoidResult(
                PaymentVoidStatus.ERROR,
                posProvider,
                null,
                null,
                responseCode,
                responseMessage,
                normalizePosOperationFailureCode(responseCode),
                responseMessage
        );
    }

    PaymentRefundResult refundError(String responseCode, String responseMessage) {
        return new PaymentRefundResult(
                PaymentRefundStatus.ERROR,
                posProvider,
                null,
                null,
                responseCode,
                responseMessage,
                normalizePosOperationFailureCode(responseCode),
                responseMessage
        );
    }

    private PaymentAuthorizationStatus normalizeAuthorizationStatus(String status) {
        if ("APPROVED".equals(status) || "AUTHORIZED".equals(status)) {
            return PaymentAuthorizationStatus.AUTHORIZED;
        }
        if ("DECLINED".equals(status)) {
            return PaymentAuthorizationStatus.DECLINED;
        }
        if ("PENDING_3DS".equals(status)) {
            return PaymentAuthorizationStatus.PENDING;
        }
        return PaymentAuthorizationStatus.ERROR;
    }

    private PaymentCaptureStatus normalizeCaptureStatus(String status, Boolean approved) {
        if ("CAPTURED".equals(status) || Boolean.TRUE.equals(approved)) {
            return PaymentCaptureStatus.SUCCEEDED;
        }
        if ("FAILED".equals(status) || Boolean.FALSE.equals(approved)) {
            return PaymentCaptureStatus.FAILED;
        }
        return PaymentCaptureStatus.ERROR;
    }

    private PaymentVoidStatus normalizeVoidStatus(String status, Boolean approved) {
        if ("VOIDED".equals(status) || Boolean.TRUE.equals(approved)) {
            return PaymentVoidStatus.SUCCEEDED;
        }
        if ("FAILED".equals(status) || Boolean.FALSE.equals(approved)) {
            return PaymentVoidStatus.FAILED;
        }
        return PaymentVoidStatus.ERROR;
    }

    private PaymentRefundStatus normalizeRefundStatus(String status, Boolean approved) {
        if ("REFUNDED".equals(status) || Boolean.TRUE.equals(approved)) {
            return PaymentRefundStatus.SUCCEEDED;
        }
        if ("FAILED".equals(status) || Boolean.FALSE.equals(approved)) {
            return PaymentRefundStatus.FAILED;
        }
        return PaymentRefundStatus.ERROR;
    }

    private String normalizeAuthorizationFailureCode(PaymentAuthorizationStatus status, String responseCode) {
        if (status == PaymentAuthorizationStatus.DECLINED) {
            return switch (responseCode) {
                case "05" -> PaymentFailureCode.DO_NOT_HONOR.name();
                case "14" -> PaymentFailureCode.INVALID_CARD_NUMBER.name();
                case "41", "43" -> PaymentFailureCode.LOST_OR_STOLEN_CARD.name();
                case "51" -> PaymentFailureCode.INSUFFICIENT_FUNDS.name();
                case "54" -> PaymentFailureCode.EXPIRED_CARD.name();
                case "57" -> PaymentFailureCode.TRANSACTION_NOT_PERMITTED.name();
                case "61", "65" -> PaymentFailureCode.LIMIT_EXCEEDED.name();
                default -> PaymentFailureCode.UNKNOWN_POS_ERROR.name();
            };
        }

        return normalizePosOperationFailureCode(responseCode);
    }

    private String normalizePosOperationFailureCode(String responseCode) {
        return switch (responseCode) {
            case "30", "13" -> PaymentFailureCode.POS_FORMAT_ERROR.name();
            case "58" -> PaymentFailureCode.POS_CONFIGURATION_ERROR.name();
            case "91" -> PaymentFailureCode.ISSUER_UNAVAILABLE.name();
            case "96" -> PaymentFailureCode.POS_SYSTEM_ERROR.name();
            case "TIMEOUT" -> PaymentFailureCode.POS_TIMEOUT.name();
            default -> PaymentFailureCode.UNKNOWN_POS_ERROR.name();
        };
    }

    private String defaultIfBlank(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
