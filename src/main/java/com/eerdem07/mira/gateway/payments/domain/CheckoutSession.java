package com.eerdem07.mira.gateway.payments.domain;

import com.eerdem07.mira.gateway.payments.domain.exception.*;
import lombok.Getter;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class CheckoutSession {

    private final UUID id;
    private final UUID paymentIntentId;
    private final String token;
    private CheckoutSessionStatus status;
    private final String returnUrl;
    private final String cancelUrl;
    private final Instant expiresAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant submittedAt;
    private Instant canceledAt;

    private CheckoutSession(UUID id, UUID paymentIntentId, String token, CheckoutSessionStatus status,
                            String returnUrl, String cancelUrl, Instant expiresAt, Instant createdAt,
                            Instant updatedAt, Instant submittedAt, Instant canceledAt) {
        this.id = id;
        this.paymentIntentId = paymentIntentId;
        this.token = token;
        this.status = status;
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.submittedAt = submittedAt;
        this.canceledAt = canceledAt;
    }

    public static CheckoutSession create(UUID id, UUID paymentIntentId, String token, 
                                         String returnUrl, String cancelUrl, 
                                         Instant expiresAt, Instant now) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(paymentIntentId, "paymentIntentId must not be null");
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        Objects.requireNonNull(now, "now must not be null");

        validateAbsoluteUrl(returnUrl, "returnUrl");
        validateAbsoluteUrl(cancelUrl, "cancelUrl");

        if (expiresAt.isBefore(now)) {
            throw new InvalidCheckoutSessionExpirationException("Expiration time cannot be in the past.");
        }

        return new CheckoutSession(
                id, paymentIntentId, token, CheckoutSessionStatus.OPEN,
                returnUrl, cancelUrl, expiresAt, now, now, null, null
        );
    }

    public static CheckoutSession restore(UUID id, UUID paymentIntentId, String token, CheckoutSessionStatus status,
                                          String returnUrl, String cancelUrl, Instant expiresAt, Instant createdAt,
                                          Instant updatedAt, Instant submittedAt, Instant canceledAt) {
        // Veritabanından (Persistence Adapter üzerinden) nesneyi yeniden oluşturmak için kullanılır.
        return new CheckoutSession(id, paymentIntentId, token, status, returnUrl, cancelUrl, 
                                   expiresAt, createdAt, updatedAt, submittedAt, canceledAt);
    }

    public boolean isExpiredAt(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        return !now.isBefore(expiresAt);
    }

    public void validateAccessibleAt(Instant now) {
        if (this.status == CheckoutSessionStatus.SUBMITTED) {
            throw new CheckoutSessionAlreadySubmittedException("Checkout session has already been submitted.");
        }
        if (this.status == CheckoutSessionStatus.CANCELED) {
            throw new CheckoutSessionCanceledException("Checkout session has been canceled.");
        }
        if (this.status == CheckoutSessionStatus.EXPIRED || isExpiredAt(now)) {
            throw new CheckoutSessionExpiredException("Checkout session has expired.");
        }
    }

    public void validateOpenAt(Instant now) {
        if (this.status != CheckoutSessionStatus.OPEN) {
            throw new CheckoutSessionNotOpenException("Checkout session is not open. Current status: " + this.status);
        }
        if (isExpiredAt(now)) {
            throw new CheckoutSessionExpiredException("Checkout session has expired.");
        }
    }

    public void submit(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        validateOpenAt(now);
        
        this.status = CheckoutSessionStatus.SUBMITTED;
        this.submittedAt = now;
        this.updatedAt = now;
    }

    public void cancel(Instant now) {
        Objects.requireNonNull(now, "now must not be null");

        if (this.status == CheckoutSessionStatus.CANCELED) {
            return;
        }

        if (this.status != CheckoutSessionStatus.OPEN) {
            throw new CheckoutSessionCannotBeCanceledException(this.status);
        }

        this.status = CheckoutSessionStatus.CANCELED;
        this.canceledAt = now;
        this.updatedAt = now;
    }

    public void expire(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        if (this.status != CheckoutSessionStatus.OPEN) {
            throw new CheckoutSessionNotOpenException("Only open checkout sessions can be expired.");
        }
        if (!isExpiredAt(now)) {
            throw new CheckoutSessionNotYetExpiredException("Cannot expire session before its expiration time.");
        }
        
        this.status = CheckoutSessionStatus.EXPIRED;
        this.updatedAt = now;
    }

    private static void validateAbsoluteUrl(String url, String fieldName) {
        if (url == null || url.isBlank()) {
            throw new InvalidCheckoutSessionUrlException(fieldName + " cannot be null or blank.");
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!uri.isAbsolute() || scheme == null || 
               (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new InvalidCheckoutSessionUrlException(fieldName + " must be a valid absolute HTTP or HTTPS URL.");
            }
        } catch (URISyntaxException e) {
            throw new InvalidCheckoutSessionUrlException(fieldName + " is malformed.", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckoutSession that = (CheckoutSession) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}