# ADR-003: Backend Framework Selection (Spring Boot)

## Status
Accepted

## Context
The domain model features intricate business logic: individual joining-date-based billing cycles, concurrency-protected seat reservations, payment signature verifications, scheduled background jobs, and fine-grained role-based security.

## Decision
Choose **Spring Boot 3.3.x (Java 21+)** as a modular monolith over Django or Node.js.

## Rationale
- Strong transactional boundaries (`@Transactional`) ensuring ACID compliance.
- Spring Data JPA with row-level pessimistic locking (`PESSIMISTIC_WRITE`) guarantees that race conditions during simultaneous seat selection cannot double-book a desk.
- Robust built-in task scheduler (`@Scheduled`) for daily billing transitions, grace periods, and timeout sweeps.
- Mature Spring Security filter chain with JWT and BCrypt hashing.
