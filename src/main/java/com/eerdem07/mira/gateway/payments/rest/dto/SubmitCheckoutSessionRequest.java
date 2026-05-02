package com.eerdem07.mira.gateway.payments.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubmitCheckoutSessionRequest(
        @NotBlank(message = "cardNumber is required")
        @Pattern(regexp = "\\d{12,19}", message = "cardNumber must contain 12 to 19 digits")
        String cardNumber,

        @NotBlank(message = "expiryMonth is required")
        @Pattern(regexp = "0[1-9]|1[0-2]", message = "expiryMonth must be between 01 and 12")
        String expiryMonth,

        @NotBlank(message = "expiryYear is required")
        @Pattern(regexp = "\\d{4}", message = "expiryYear must be a 4-digit year")
        String expiryYear,

        @NotBlank(message = "cvc is required")
        @Pattern(regexp = "\\d{3,4}", message = "cvc must contain 3 or 4 digits")
        String cvc,

        @NotBlank(message = "cardHolderName is required")
        @Size(max = 200, message = "cardHolderName must not exceed 200 characters")
        String cardHolderName
) {}
