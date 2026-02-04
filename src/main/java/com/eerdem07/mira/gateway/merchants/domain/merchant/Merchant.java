package com.eerdem07.mira.gateway.merchants.domain.merchant;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Merchant {
    private final UUID merchantId;
    private final String legalName;
    private MerchantStatus status;
    private final Instant createdAt;
    private Instant activatedAt; // nullable
    private Instant suspendedAt; // nullable

    private Merchant(UUID merchantId, String legalName, MerchantStatus status, Instant createdAt, Instant activatedAt, Instant suspendedAt) {
        this.merchantId = Objects.requireNonNull(merchantId);
        this.legalName = validateLegalName(legalName);
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.activatedAt = activatedAt;
        this.suspendedAt = suspendedAt;

        if(this.status == MerchantStatus.ACTIVE && this.activatedAt == null){
            throw new IllegalStateException("ACTIVE merchant must be activatedAt");
        }

        if(this.status == MerchantStatus.SUSPENDED && this.suspendedAt == null){
            throw new IllegalStateException("SUSPENDED merchant must be suspendedAt");
        }
    }

    public static Merchant register(UUID merchantId, String legalName, Clock clock){
        Instant now = Instant.now(Objects.requireNonNull(clock, "clock"));

        return new Merchant(merchantId, legalName, MerchantStatus.PENDING, now, null, null);
    }

    private static String validateLegalName(String legalName) {
        if(legalName == null) throw new IllegalArgumentException("legalName cannot be null");
        String v = legalName.trim();
        if(v.isEmpty()) throw new IllegalArgumentException("legalName cannot be empty");
        if(v.length() > 200) throw new IllegalArgumentException("legalName is too long (max 200)");
        return v;
    }

    public void activate(Clock clock){
        if(!status.canActivate()){
            throw new IllegalStateException("Only PENDING merchant can be activated! Current =" + status);
        }

        this.status = MerchantStatus.ACTIVE;
        this.activatedAt = Instant.now(Objects.requireNonNull(clock,"clock"));
        this.suspendedAt = null;
    }

    public void suspend(Clock clock){
        if(!status.canSuspend()){
            throw new IllegalStateException();
        }

        this.status = MerchantStatus.SUSPENDED;
        this.suspendedAt = Instant.now(Objects.requireNonNull(clock, "clock"));
    }

    // GETTERS - Domain'de SET YOK!
    public UUID getMerchantId(){ return merchantId; }
    public String getLegalName(){ return legalName; }
    public MerchantStatus getStatus(){ return status;}
    public Instant getCreatedAt(){return createdAt;}
    public Optional<Instant> getActivatedAt(){return Optional.ofNullable(activatedAt);}
    public Optional<Instant> getSuspendedAt(){return Optional.ofNullable(suspendedAt);}

}


