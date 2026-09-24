# Sanjay Library - Documentation Hub
### Digital Library Management System for Sanjay Library

Welcome to the official engineering documentation for **Sanjay Library**, a production-grade mobile-first platform built for Sanjay (Library Owner) and his physical digital and self-study library students.

## Table of Contents

### 1. Assessment & Strategy
- [Project Assessment](file:///home/afsarai/Library-Application/docs/assessment/project-assessment.md) - Baseline assessment, missing components, phases, assumptions, and risks.

### 2. Product Specifications
- [Product Overview](file:///home/afsarai/Library-Application/docs/product/product-overview.md) - Vision, value proposition, and physical library context.
- [Requirements](file:///home/afsarai/Library-Application/docs/product/requirements.md) - Detailed functional and non-functional requirements.
- [User Roles & Permissions](file:///home/afsarai/Library-Application/docs/product/user-roles.md) - Role-Based Access Control (Student, Admin/Owner, Staff, Super Admin).
- [User Flows](file:///home/afsarai/Library-Application/docs/product/user-flows.md) - End-to-end user journeys (Admission, Check-in, Payment, Seat Change).
- [Business Rules](file:///home/afsarai/Library-Application/docs/product/business-rules.md) - Joining-date billing cycles, grace periods, attendance blocks, seat release.

### 3. Architecture & Design
- [Architecture Overview](file:///home/afsarai/Library-Application/docs/architecture/architecture-overview.md) - Modular monolith philosophy and high-level structure.
- [System Architecture](file:///home/afsarai/Library-Application/docs/architecture/system-architecture.md) - Component topology, infrastructure, network security.
- [High-Level Design (HLD)](file:///home/afsarai/Library-Application/docs/architecture/hld.md) - System context, interaction flows, sequence diagrams.
- [Low-Level Design (LLD)](file:///home/afsarai/Library-Application/docs/architecture/lld.md) - Class diagrams, services, DTOs, state machines, transaction boundaries.

### 4. Database & Persistence
- [Database Design](file:///home/afsarai/Library-Application/docs/database/database-design.md) - Relational architecture, multi-tenant partitioning, indexing.
- [ER Diagram](file:///home/afsarai/Library-Application/docs/database/er-diagram.md) - Entity Relationship visual diagram with cardinalities.
- [Data Dictionary](file:///home/afsarai/Library-Application/docs/database/data-dictionary.md) - Comprehensive table and column specifications.
- [Database Migrations](file:///home/afsarai/Library-Application/docs/database/migrations.md) - Flyway migration strategy and versioning conventions.

### 5. API Documentation
- [API Overview & Conventions](file:///home/afsarai/Library-Application/docs/api/api-overview.md) - Versioning, base URLs, standard envelopes, error handling.
- [Authentication API](file:///home/afsarai/Library-Application/docs/api/authentication.md) - Login, register, token refresh, logout endpoints.
- [Seat & Admission API](file:///home/afsarai/Library-Application/docs/api/seat-api.md) - Layout fetching, temporary reservation, admission submission.
- [Attendance API](file:///home/afsarai/Library-Application/docs/api/attendance-api.md) - Rotating QR token generation, check-in, checkout, history.
- [Subscription & Billing API](file:///home/afsarai/Library-Application/docs/api/subscription-api.md) - Cycles, due dates, admin extensions.
- [Payment API](file:///home/afsarai/Library-Application/docs/api/payment-api.md) - Razorpay order creation, verification, webhooks, cash recordings.

### 6. Mobile Application (Flutter)
- [Flutter Architecture](file:///home/afsarai/Library-Application/docs/mobile/flutter-architecture.md) - Feature-first directory structure and layers.
- [Navigation & Routing](file:///home/afsarai/Library-Application/docs/mobile/navigation.md) - GoRouter implementation with authentication guards.
- [State Management](file:///home/afsarai/Library-Application/docs/mobile/state-management.md) - Riverpod architecture and state lifecycles.
- [Offline Resilience](file:///home/afsarai/Library-Application/docs/mobile/notification-architecture.md) - Token caching, network interceptors, offline states.

### 7. Backend Services (Spring Boot)
- [Spring Boot Architecture](file:///home/afsarai/Library-Application/docs/backend/spring-boot-architecture.md) - Modular monolith patterns.
- [Domain Modules](file:///home/afsarai/Library-Application/docs/backend/modules.md) - Separation of concerns and module responsibilities.
- [Security Architecture](file:///home/afsarai/Library-Application/docs/backend/security.md) - Spring Security, stateless JWT, password hashing, RBAC.
- [Scheduled Jobs](file:///home/afsarai/Library-Application/docs/backend/scheduled-jobs.md) - Billing crons, reservation cleanup, notification dispatches.

### 8. Payments & Webhooks
- [Razorpay Integration](file:///home/afsarai/Library-Application/docs/payments/razorpay-integration.md) - SDK integration, keys, security practices.
- [Payment Lifecycle](file:///home/afsarai/Library-Application/docs/payments/payment-lifecycle.md) - States: Created, Authorized, Captured, Failed, Refunded.
- [Webhook Handling & Idempotency](file:///home/afsarai/Library-Application/docs/payments/webhook-handling.md) - Signature verification and deduplication.

### 9. Notifications
- [Notification Engine](file:///home/afsarai/Library-Application/docs/notifications/notification-system.md) - Provider-agnostic notification dispatch architecture.
- [Push Notifications (FCM)](file:///home/afsarai/Library-Application/docs/notifications/push-notifications.md) - Device token registration and message payloads.

### 10. Operations & Deployment
- [Local Development Setup](file:///home/afsarai/Library-Application/docs/deployment/development-setup.md) - Running locally with Docker Compose, Maven, and Flutter.
- [Production Deployment](file:///home/afsarai/Library-Application/docs/deployment/production-deployment.md) - VPS/Cloud hosting, SSL, Docker Compose.
- [Environment Variables](file:///home/afsarai/Library-Application/docs/deployment/environment-variables.md) - Full specification of all configuration keys.
- [Admin Operating Guide](file:///home/afsarai/Library-Application/docs/operations/admin-guide.md) - Guide for library owners on daily operations.

### 11. Architectural Decision Records (ADRs)
- [ADR-001: Technology Stack](file:///home/afsarai/Library-Application/docs/decisions/ADR-001-technology-stack.md)
- [ADR-002: Flutter for Mobile](file:///home/afsarai/Library-Application/docs/decisions/ADR-002-mobile-framework.md)
- [ADR-003: Spring Boot for Backend](file:///home/afsarai/Library-Application/docs/decisions/ADR-003-backend-framework.md)
- [ADR-004: PostgreSQL Relational Database](file:///home/afsarai/Library-Application/docs/decisions/ADR-004-database.md)
- [ADR-005: Razorpay Payment Gateway](file:///home/afsarai/Library-Application/docs/decisions/ADR-005-payment-gateway-razorpay.md)
- [ADR-006: Riverpod for State Management](file:///home/afsarai/Library-Application/docs/decisions/ADR-006-state-management-riverpod.md)
