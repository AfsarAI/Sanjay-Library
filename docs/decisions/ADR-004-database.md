# ADR-004: Relational Database Selection (PostgreSQL)

## Status
Accepted

## Context
The domain is strongly relational: Library -> Seats, Library -> Students -> Subscriptions -> Payments -> Attendance Records. Data loss or corruption in payments or seat allocation is unacceptable.

## Decision
Select **PostgreSQL 16** with Flyway database migration versioning.

## Rationale
- Native row-level locking (`SELECT ... FOR UPDATE`) prevents concurrent seat reservation race conditions.
- Strict foreign key constraints and check constraints enforce data integrity at the database engine level.
- Native `TIMESTAMPTZ` and `NUMERIC` types ensure precision in billing amounts and timestamps.
- Zero-cost, enterprise-grade open-source license.
