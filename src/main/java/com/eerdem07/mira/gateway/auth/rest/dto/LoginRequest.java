package com.eerdem07.mira.gateway.auth.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(@NotBlank(message = "email boş bırakılamaz")
                           @Size(min = 12, max = 20, message = "email must be bigger then 20") String email,

                           @NotBlank
                           @Size()
                           String password) {
}

// email'i toLowerCase, trim yap.
