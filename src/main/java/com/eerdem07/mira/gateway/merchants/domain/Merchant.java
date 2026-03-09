package com.eerdem07.mira.gateway.merchants.domain;

import com.eerdem07.mira.gateway.shared.domain.EmailValidator;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Getter
public class Merchant {
    private final UUID merchantId;
    private final String legalName;
    private final Instant createdAt;
    private final String email;
    private final String passwordHash;
    private MerchantStatus status;
    private Instant activatedAt; // nullable
    private Instant suspendedAt; // nullable

    private Merchant(UUID merchantId, String email, String password, String legalName, MerchantStatus status, Instant createdAt, Instant activatedAt, Instant suspendedAt) {
        this.merchantId = Objects.requireNonNull(merchantId);
        this.legalName = validateLegalName(legalName);
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.activatedAt = activatedAt;
        this.suspendedAt = suspendedAt;
        this.email = Objects.requireNonNull(email, "email");
        this.passwordHash = Objects.requireNonNull(password, "passwordHash");

        if (this.status == MerchantStatus.ACTIVE && this.activatedAt == null) {
            throw new IllegalStateException("ACTIVE merchant must be activatedAt");
        }

        if (this.status == MerchantStatus.SUSPENDED && this.suspendedAt == null) {
            throw new IllegalStateException("SUSPENDED merchant must be suspendedAt");
        }
    }

    public static Merchant register(UUID merchantId, String email, String passwordHash, String legalName, Instant now) {
        if (!EmailValidator.isValid(email)) {
//            throw new InvalidEmailException("Invalid email format");
        }

        return new Merchant(merchantId, email.trim()
                .toLowerCase(), passwordHash, legalName, MerchantStatus.PENDING,
                Objects.requireNonNull(now, "now"), null, null);
    }

    private static String validateLegalName(String legalName) {
        if (legalName == null) throw new IllegalArgumentException("legalName cannot be null");
        String v = legalName.trim();
        if (v.isEmpty()) throw new IllegalArgumentException("legalName cannot be empty");
        if (v.length() > 200) throw new IllegalArgumentException("legalName is too long (max 200)");
        return v;
    }

    public static Merchant restore(UUID merchantId, String email, String passwordHash, String legalName, MerchantStatus status, Instant createdAt, Instant activatedAt, Instant suspendedAt) {
        return new Merchant(merchantId, email, passwordHash, legalName, status, createdAt, activatedAt, suspendedAt);
    }

    public void activate(Instant now) {
        if (!status.canActivate()) throw new IllegalStateException("Only PENDING...");
        this.status = MerchantStatus.ACTIVE;
        this.activatedAt = Objects.requireNonNull(now, "now");
        this.suspendedAt = null;
    }

    public void suspend(Instant now) {
        if (!status.canSuspend()) throw new IllegalStateException("...");
        this.status = MerchantStatus.SUSPENDED;
        this.suspendedAt = Objects.requireNonNull(now, "now");
    }

    // GETTERS - Domain'de SET YOK!

    public Optional<Instant> getActivatedAt() {
        return Optional.ofNullable(activatedAt);
    }

    public Optional<Instant> getSuspendedAt() {
        return Optional.ofNullable(suspendedAt);
    }

}


