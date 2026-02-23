package com.eerdem07.mira.gateway.shared.domain;

import java.util.regex.Pattern;

public class EmailValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}$",
            Pattern.CASE_INSENSITIVE
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null) return false;

        String value = email.trim();
        if (value.isEmpty() || value.length() > 254) return false;

        int at = value.indexOf('@');
        if (at <= 0 || at != value.lastIndexOf('@') || at == value.length() - 1) {
            return false;
        }

        String localPart = value.substring(0, at);
        String domainPart = value.substring(at + 1);

        if (localPart.length() > 64) return false;
        if (domainPart.startsWith(".") || domainPart.endsWith(".")) return false;
        if (domainPart.contains("..")) return false;

        return EMAIL_PATTERN.matcher(value)
                .matches();
    }

}
