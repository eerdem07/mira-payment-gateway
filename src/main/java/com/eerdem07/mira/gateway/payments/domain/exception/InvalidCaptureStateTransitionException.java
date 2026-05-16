package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.payments.domain.CaptureStatus;
import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class InvalidCaptureStateTransitionException extends DomainException {

    public InvalidCaptureStateTransitionException(CaptureStatus currentStatus, CaptureStatus targetStatus) {
        super(
                "INVALID_CAPTURE_STATE_TRANSITION",
                "Capture cannot transition from " + currentStatus + " to " + targetStatus + "."
        );
    }
}
