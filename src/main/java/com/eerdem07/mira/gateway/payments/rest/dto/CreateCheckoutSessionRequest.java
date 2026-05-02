package com.eerdem07.mira.gateway.payments.rest.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record CreateCheckoutSessionRequest(
        @NotBlank(message = "returnUrl is required")
        @URL(message = "returnUrl must be a valid URL")
        String returnUrl,

        @NotBlank(message = "cancelUrl is required")
        @URL(message = "cancelUrl must be a valid URL")
        String cancelUrl
) {}
