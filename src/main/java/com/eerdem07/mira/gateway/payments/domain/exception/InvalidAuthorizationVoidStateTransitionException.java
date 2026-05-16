package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.payments.domain.AuthorizationVoidStatus;
import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class InvalidAuthorizationVoidStateTransitionException extends DomainException {

    public InvalidAuthorizationVoidStateTransitionException(AuthorizationVoidStatus currentStatus,
                                                           AuthorizationVoidStatus targetStatus) {
        super(
                "INVALID_AUTHORIZATION_VOID_STATE_TRANSITION",
                "Authorization void cannot transition from " + currentStatus + " to " + targetStatus + "."
        );
    }
}
