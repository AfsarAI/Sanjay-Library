# Flutter Mobile Application Architecture

## 1. Architectural Principles
The Digital Library mobile client is engineered using a **Feature-First Clean Architecture** with **Riverpod 2** for declarative state management, **Dio** for robust HTTP/REST communication, and **GoRouter** for declarative navigation with auth-guard redirects.

```
mobile/lib/
  ├── core/
  │   ├── config/          # AppConfig, API base URLs, timeouts, constants
  │   ├── constants/       # Semantic AppColors (Emerald, Amber, Coral, Slate)
  │   ├── errors/          # ApiException & central error mapper
  │   ├── network/         # Dio ApiClient with automatic JWT Bearer injection & 401 refresh
  │   ├── providers/       # Dependency Injection providers for Riverpod
  │   ├── routing/         # GoRouter configuration with role and auth redirect guards
  │   ├── security/        # TokenStorage backed by flutter_secure_storage (Keychain / KeyStore)
  │   ├── theme/           # Dark Theme design system with GoogleFonts Inter typography
  │   └── utils/           # Formatters (INR currency, date/time, session duration)
  ├── features/
  │   ├── admin/           # Owner Dashboard, metrics cards, cash payment dialog, grace extensions
  │   ├── admission/       # Admission wizard with joining-date calendar & desk hold confirmation
  │   ├── attendance/      # Dynamic QR scanner with MobileScanner & history logs
  │   ├── authentication/  # Login & Register screens with demo quick-fill presets
  │   ├── dashboard/       # Student home with active desk badge, validity countdown, & quick actions
  │   ├── payments/        # Razorpay online checkout simulator and receipt ledger
  │   ├── seats/           # Interactive 2D Visual Seat Layout (A01 - A50) with selection sheet
  │   └── subscriptions/   # Joining-date monthly billing cycle state notifier
  ├── models/              # Immutable domain entities with JSON serialization & business getters
  ├── services/            # API communication services
  └── main.dart            # ProviderScope entrypoint with dark theme & GoRouter
```

---

## 2. Key Technical Highlights

### A. Real-Time 2D Seat Map Grid
- Renders the physical library layout of 50 desks (A01–A50).
- Dynamically colors desks based on backend database state:
  - **Emerald Green**: Available
  - **Coral Red**: Occupied
  - **Amber Gold**: Temporarily Reserved (10-minute hold)
  - **Slate Grey**: Under Maintenance
  - **Electric Sky Blue**: User Selected
- Features pan & scroll support with quick status metrics bar and interactive booking bottom sheet.

### B. Hardware-Backed Dynamic QR Scanner
- Integrated with `mobile_scanner` for fast barcode detection.
- Renders a neon reticle box overlay.
- Automatically captures the 30-second rotating cryptographic QR token from the library's physical terminal or kiosk.
- Fallback simulation button allows end-to-end testing in headless environments or developer emulators.

### C. Secure Storage & Auto Token Refresh
- JWT access tokens and refresh tokens are stored using `FlutterSecureStorage` (AES hardware-backed KeyStore on Android, Keychain on iOS).
- When any protected API call encounters a `401 Unauthorized`, the Dio interceptor automatically initiates `/auth/refresh-token`, saves the new access token, and retries the original request without user interruption.

### D. Joining-Date Monthly Billing Cycle
- Calculates subscription cycles anchored strictly to the student's admission date (e.g. 15 Aug to 14 Sep).
- Tracks overdue days and enforces attendance blockage rules gracefully in the UI.
