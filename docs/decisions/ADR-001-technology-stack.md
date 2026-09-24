# ADR-001: Technology Stack Selection

## Status
Accepted

## Context
The client requires a production-grade digital library management system to manage seats, admissions, attendance, subscriptions, and payments for a physical study library. The system must operate reliably with low operational overhead and be maintainable by a small engineering team while supporting future scalability.

## Decision
We select:
- **Mobile Frontend:** Flutter with Dart
- **Backend:** Spring Boot (Java 21+)
- **Database:** PostgreSQL 16
- **Architecture:** Modular Monolith
- **Payments:** Razorpay API & Webhooks
- **Push Notifications:** Firebase Cloud Messaging (FCM)
- **Deployment:** Docker & Docker Compose on Linux VPS

## Consequences
- Enables a single codebase for Android (primary) and future iOS.
- Delivers ACID relational guarantees for financial transactions and seat locks.
- Minimizes hosting and maintenance costs compared to Kubernetes/microservices.
