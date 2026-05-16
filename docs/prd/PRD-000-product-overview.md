# PRD-000: Mira Payment Gateway - Product Overview

## 1. Overview

Mira Payment Gateway is a portfolio-grade payment gateway simulation project designed to model core payment system concepts such as merchant onboarding, API credentials, hosted checkout, payment authorization, refunds, ledger, and payouts.

The system allows merchants to create payment intents, redirect customers to a hosted checkout page, process payments through a mock POS provider, and track payment lifecycle states.

This project does not process real payments and does not store real card data.

## 2. Problem Statement

Building a payment system requires handling complex flows such as merchant authentication, payment session creation, card payment authorization, payment state management, refunds, settlement, and merchant payouts.

Mira Payment Gateway aims to model these concepts in a clean, modular, and realistic way so that the project demonstrates both backend engineering skills and payment domain understanding.

## 3. Product Goals

- Provide a realistic payment gateway simulation.
- Support merchant onboarding and API credential generation.
- Allow merchants to create PaymentIntents.
- Allow merchants to create CheckoutSessions for hosted checkout.
- Process basic card payments through a Mock POS integration.
- Support future flows such as 3DS, installments, preauthorization, refunds, ledger, and payouts.
- Demonstrate clean architecture, domain-driven design, and secure API design.
- Avoid storing real card data or processing real payments.

## 4. Non-Goals / Out of Scope

- Real PSP or bank integration.
- Real card payment processing.
- Real card data storage.
- PCI DSS compliance.
- Production-grade fraud detection.
- Real settlement with banks.
- Real money movement.
- Multi-currency FX conversion.
- Full merchant dashboard in the initial version.

## 5. Target Users / Actors

### Merchant

A business that integrates with Mira Payment Gateway to accept simulated payments.

### Customer

The end user who is redirected from the merchant website to the Mira hosted checkout page.

### Mira Gateway

The backend system responsible for payment intent creation, checkout session management, payment processing, payment lifecycle tracking, and merchant-facing APIs.

### Mock POS

A simulated POS provider used to approve or decline payment authorization requests.

## 6. Core Modules

### Merchant Module

Handles merchant registration, merchant profile management, and merchant identity.

### API Credential Module

Handles API key generation, secret verification, and merchant server-to-server authentication.

### Payment Module

Handles PaymentIntent, CheckoutSession, PaymentAttempt, and payment lifecycle transitions.

### Hosted Checkout Module

Provides the customer-facing payment page where customers enter payment details.

### Mock POS Integration

Simulates bank/PSP authorization behavior.

### Refund Module

Handles refund requests after successful payments.

### Ledger Module

Tracks financial movements and merchant balances.

### Payout Module

Handles simulated merchant withdrawals/payouts based on available balance.

## 7. Product Roadmap

### PRD-001: Hosted Checkout - Basic Card Payment

Basic card payment flow without 3DS, installments, or preauthorization.

### PRD-002: 3DS Authentication

Adds redirect/challenge-based customer authentication flow.

### PRD-003: Installments

Adds installment selection and installment-aware payment processing.

### PRD-004: Preauthorization, Capture and Void

Adds authorization-only payments, later capture, and void support.

### PRD-005: Refunds

Adds full and partial refund capabilities.

### PRD-006: Ledger

Adds financial transaction tracking and merchant balance calculation.

### PRD-007: Payouts

Adds simulated merchant payout/withdrawal flow.

## 8. High-Level Payment Flow

1. Merchant registers in Mira.
2. Merchant generates API credentials.
3. Merchant creates a PaymentIntent.
4. Merchant creates a CheckoutSession for the PaymentIntent.
5. Mira returns a checkout URL.
6. Merchant redirects the customer to the hosted checkout page.
7. Customer submits payment details.
8. Mira creates a PaymentAttempt.
9. Mira sends authorization request to Mock POS.
10. Mock POS approves or declines the payment.
11. Mira updates payment states.
12. Mira redirects customer back to merchant.
13. Mira sends webhook notification to merchant.

## 9. Success Criteria

- A merchant can complete the basic payment flow end-to-end.
- Payment lifecycle states are consistent and traceable.
- API credentials are handled securely.
- Raw card data is not stored.
- PaymentAttempt records are created for each payment try.
- Mock POS integration can approve and decline payments.
- The system can be extended to support 3DS, installments, refunds, ledger, and payouts.

## 10. References

- PRD-001: Hosted Checkout - Basic Card Payment
- PRD-002: 3DS Authentication
- PRD-003: Installments
- PRD-004: Preauthorization, Capture and Void
- PRD-005: Refunds
- PRD-006: Ledger
- PRD-007: Payouts