package com.eerdem07.mira.gateway.shared.exception;

public class MissingJwtClaimException extends ApplicationException {
    public MissingJwtClaimException(String claim) {
        super(ErrorType.UNAUTHORIZED, "JWT_CLAIM_MISSING", "This claim is missing: " + claim);
    }
}
