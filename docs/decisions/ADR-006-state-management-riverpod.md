# ADR-006: Flutter State Management (Riverpod)

## Status
Accepted

## Context
The Flutter mobile application manages complex states: authentication tokens, real-time visual seat availability maps, admission wizard steps, camera QR scanning sessions, subscription renewals, and admin operational feeds.

## Decision
Adopt **Flutter Riverpod** as the state management framework.

## Rationale
- Compile-time safety: Providers are declared globally as immutable objects, eliminating runtime `ProviderNotFoundException`.
- Decoupled from `BuildContext`: Business logic and repositories can be invoked and tested without mocking widget trees.
- `AsyncNotifier` and `StateNotifier` provide built-in primitives for loading, error, and data states.
