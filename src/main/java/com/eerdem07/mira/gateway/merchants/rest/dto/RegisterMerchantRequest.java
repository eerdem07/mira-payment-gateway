package com.eerdem07.mira.gateway.merchants.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record RegisterMerchantRequest(
        @NotBlank @Size(min = 2, max = 200) String legalName,
        @NotBlank @Email @Size(max = 200) String email,
        @NotBlank @Size(min = 12, max = 200)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).+$",
                message = "password must contain upper, lower, digit, special")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password,
        @NotBlank @Size(min = 12, max = 200)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String passwordConfirm
) {
    @AssertTrue(message = "password and confirmation must match")
    public boolean isPasswordMatching() {
        return password != null && password.equals(passwordConfirm);
    }
}
