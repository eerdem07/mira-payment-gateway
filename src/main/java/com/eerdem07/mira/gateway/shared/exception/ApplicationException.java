package com.eerdem07.mira.gateway.shared.exception;

import lombok.Getter;

@Getter
public abstract class ApplicationException extends RuntimeException {

    private final String code;
    private final ErrorType type;

    protected ApplicationException(ErrorType type, String code, String message) {
        super(message);
        this.type = type;
        this.code = code;
    }

    protected ApplicationException(ErrorType type, String code, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.code = code;
    }

    public enum ErrorType {
        BAD_REQUEST(400),
        UNAUTHORIZED(401),
        FORBIDDEN(403),
        NOT_FOUND(404),
        CONFLICT(409),
        UNPROCESSABLE_ENTITY(422),
        INTERNAL_SERVER_ERROR(500);

        private final int httpStatus;

        ErrorType(int httpStatus) {
            this.httpStatus = httpStatus;
        }

        public int httpStatus() {
            return httpStatus;
        }
    }

}

// Örnek kullanım:
// super(ErrorType.NOT_FOUND, "MERCHANT_NOT_FOUND", "Merchant not found: " + merchantId);