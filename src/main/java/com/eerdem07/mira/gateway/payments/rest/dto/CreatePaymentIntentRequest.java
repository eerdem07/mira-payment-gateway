package com.eerdem07.mira.gateway.payments.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePaymentIntentRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be a valid 3-letter ISO 4217 code (e.g., USD, EUR)")
        String currency,

        @NotBlank(message = "Merchant reference is required")
        @Size(max = 255, message = "Merchant reference must not exceed 255 characters")
        String merchantReference,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description
) {
}

// DTO şu an MVP için kısa tutuldu.
/*

 * CustomerDetails customer
 * - String name
 * - String email
 * - String phone

 * BillingDetails billingDetails
 * - String country
 * - String city
 * - String postalCode
 * - String addressLine1
 * - String addressLine2

 * Map<String, String> metadata
 *
 * - invoiceId:
 *   Merchant sistemindeki fatura numarası/id bilgisidir.
 * - productId:
 *   Ödemeye konu olan ürünün merchant sistemindeki id bilgisidir.
 * - merchantCustomerId:
 *   Ödemeyi yapan müşterinin merchant sistemindeki id bilgisidir.
 * - campaignCode:
 *   Ödemede kullanılan kampanya/indirim kodudur.


 * CaptureMethod captureMethod
 * - AUTOMATIC: ödeme başarılı olursa tutar doğrudan tahsil edilir.
 * - MANUAL: önce provizyon alınır, merchant daha sonra capture işlemi yapar.

 * Long expiresInSeconds
 * - PaymentIntent'in kaç saniye geçerli olacağını belirtir.
 *
 * String statementDescriptor
 * - Kart ekstresinde gözükecek kısa ödeme açıklamasıdır.


 */