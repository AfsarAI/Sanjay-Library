# Digital Library API Overview & Conventions

## Base URLs
- **Local Development**: `http://localhost:8080/api/v1`
- **Android Emulator**: `http://10.0.2.2:8080/api/v1`
- **Swagger UI Interactive Explorer**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI v3 JSON Specification**: `http://localhost:8080/v3/api-docs`

---

## Standard Response Format
All endpoints return a uniform envelope structure:

### Success Response (`200 OK`, `201 Created`)
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed successfully",
  "timestamp": "2026-09-24T12:49:08.834Z"
}
```

### Error Response (`400`, `401`, `403`, `404`, `409`, `500`)
```json
{
  "success": false,
  "error": {
    "code": "SEAT_ALREADY_RESERVED",
    "message": "Seat A23 is currently reserved by another student",
    "details": null
  },
  "timestamp": "2026-09-24T12:49:08.834Z"
}
```

---

## Authentication & Authorization
The API uses **Stateless JSON Web Tokens (JWT)**.
Clients must include the Bearer token in the `Authorization` header for protected endpoints:
```http
Authorization: Bearer <accessToken>
```

### Roles Supported:
1. `ROLE_STUDENT`: Access to own profile, admission, own attendance, own subscriptions, seat booking.
2. `ROLE_ADMIN`: Library owner with full facility management, seat release, cash logging, student suspension, and metrics.
3. `ROLE_SUPER_ADMIN`: Multi-tenant administrative platform controller.

---

## Core Endpoint Catalog

| Module | Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/api/v1/auth/login` | Public | Authenticates student or admin using phone + password |
| **Auth** | `POST` | `/api/v1/auth/register` | Public | Registers new student profile |
| **Auth** | `POST` | `/api/v1/auth/refresh-token` | Public | Exchanges refresh token for new access token |
| **Auth** | `POST` | `/api/v1/auth/logout` | Authenticated | Revokes refresh token and terminates session |
| **Seats** | `GET` | `/api/v1/seats/layout` | Public / Auth | Returns real-time 50 desks 2D visual layout map |
| **Seats** | `POST` | `/api/v1/seats/{seatId}/reserve` | Student | Places a 10-minute hold lock using pessimistic locking |
| **Admissions** | `POST` | `/api/v1/admissions` | Student | Confirms admission and activates assigned desk |
| **Admissions** | `GET` | `/api/v1/admissions/my-admission` | Student | Fetches current admission and seat number |
| **Attendance**| `GET` | `/api/v1/attendance/qr-token` | Public / Kiosk| Returns 30-second cryptographic rotating QR token |
| **Attendance**| `POST` | `/api/v1/attendance/check-in` | Student | Records check-in after verifying QR token and fees |
| **Attendance**| `POST` | `/api/v1/attendance/check-out` | Student | Closes session and records total study duration |
| **Attendance**| `GET` | `/api/v1/attendance/my-history` | Student | Paginated past study logs |
| **Subscriptions**| `GET` | `/api/v1/subscriptions/my-subscription` | Student | Active cycle, due date, and attendance clearance |
| **Subscriptions**| `POST` | `/api/v1/subscriptions/{id}/extension`| Admin | Grants grace period extension to unlock attendance |
| **Payments** | `POST` | `/api/v1/payments/create-order` | Student | Initializes Razorpay payment order for native checkout |
| **Payments** | `POST` | `/api/v1/payments/verify` | Student | Server-side HMAC-SHA256 signature verification |
| **Payments** | `POST` | `/api/v1/payments/cash` | Admin | Logs offline cash receipt and extends subscription |
| **Payments** | `GET` | `/api/v1/payments/my-history` | Student | Paginated payment transactions and digital receipts |
| **Admin** | `GET` | `/api/v1/admin/dashboard` | Admin | Real-time occupancy, inside count, overdue alerts, revenue |
| **Admin** | `POST` | `/api/v1/admin/seats/{seatId}/release`| Admin | Vacates occupied desk and releases student assignment |
| **Admin** | `POST` | `/api/v1/admin/students/{id}/change-seat`| Admin | Swaps student to a new available desk |
| **Admin** | `POST` | `/api/v1/admin/students/{id}/status` | Admin | Suspends or reactivates student account |
