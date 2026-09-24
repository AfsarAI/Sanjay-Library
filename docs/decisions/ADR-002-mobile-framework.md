# ADR-002: Mobile Framework Selection (Flutter + Dart)

## Status
Accepted

## Context
The primary user interface is mobile-first. Students and the library owner use smartphones to interact with the system. While the initial audience is 100% Android in the village/city study center, the architecture must support iOS in the future without a complete rewrite.

## Decision
Adopt **Flutter (Dart)** with Riverpod for state management, Dio for networking, GoRouter for routing, and flutter_secure_storage for sensitive credentials.

## Consequences
- High-performance 2D canvas rendering for interactive seat layout maps (A01-A50).
- Strict typed language (Dart) reduces runtime bugs.
- Cross-platform support for Android and iOS from a single repository.
