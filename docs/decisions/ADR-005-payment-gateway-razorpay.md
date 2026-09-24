# ADR-005: Payment Gateway Selection (Razorpay)

## Status
Accepted

## Context
The target customer base is situated in India, where UPI (Google Pay, PhonePe, Paytm, BHIM) is the overwhelming preference for consumer payments.

## Decision
Integrate **Razorpay India** as the primary payment processor for online transactions, complemented by manual cash entry for in-person cash payments.

## Rationale
- Native UPI intent flow on Android devices via Razorpay Standard Checkout SDK.
- Robust webhook delivery with HMAC-SHA256 signature verification.
- Direct account settlement into the library owner's merchant bank account.
- Complete support for idempotency and server-side verification.
