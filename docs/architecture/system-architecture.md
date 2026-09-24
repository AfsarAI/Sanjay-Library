# Digital Library Management System - System Architecture

## 1. Physical Deployment Topology

```mermaid
graph LR
    subgraph Users ["Client Devices"]
        StudentDevice["Student Android Phone (Flutter App)"]
        OwnerDevice["Library Owner Phone (Flutter App)"]
        Tablet["In-Library Tablet (Displaying Rotating QR)"]
    end

    subgraph CloudVPS ["Production VPS / Cloud Host"]
        subgraph DockerNet ["Docker Bridge Network"]
            Nginx["Nginx Reverse Proxy (SSL / Port 443)"]
            Backend["Spring Boot Backend (Port 8080)"]
            DB[(PostgreSQL 16 Database Port 5432)]
        end
    end

    subgraph External ["External Third-Party Services"]
        Razorpay["Razorpay Gateway (Orders / Webhooks)"]
        FCM["Firebase Cloud Messaging (FCM Push)"]
    end

    StudentDevice -->|HTTPS /api/v1| Nginx
    OwnerDevice -->|HTTPS /api/v1| Nginx
    Tablet -->|HTTPS /api/v1/attendance/qr-token| Nginx
    Nginx -->|Reverse Proxy| Backend
    Backend -->|JDBC Connection Pool (HikariCP)| DB
    Backend -->|REST API| Razorpay
    Razorpay -->|HTTPS Webhook /api/v1/payments/webhook| Nginx
    Backend -->|FCM HTTP v1 API| FCM
    FCM -->|Push Notification| StudentDevice
    FCM -->|Push Notification| OwnerDevice
```

---

## 2. Infrastructure Components

### 2.1 Reverse Proxy (Nginx)
- **SSL Termination:** Automatic Let's Encrypt certificate renewal.
- **Request Rate Limiting:** Enforces maximum 20 req/s per IP on `/api/v1/auth/*` to prevent brute force; 60 req/s on general endpoints.
- **Static Assets & Compression:** Gzip compression enabled for JSON payloads.
- **Security Headers:** HSTS, X-Content-Type-Options: nosniff, X-Frame-Options: DENY.

### 2.2 Application Container (Spring Boot)
- **Runtime:** Eclipse Temurin / Oracle JDK 25 / 21 LTS with optimized JVM flags (`-XX:+UseG1GC -XX:+UseStringDeduplication -Xms256m -Xmx768m`).
- **Connection Pool:** HikariCP with max 10 active database connections (adequate for 100–500 active users, minimal memory footprint).
- **Embedded Web Server:** Apache Tomcat with HTTP/2 support.

### 2.3 Database Engine (PostgreSQL 16)
- **Storage:** Persistent Docker volume mounted to host NVMe storage.
- **Backups:** Automated nightly `pg_dump` crons with 14-day retention and S3/offsite sync.
- **Collation & Encoding:** `UTF-8`, `en_US.UTF-8` locale.
- **Timezone:** System timestamps stored strictly in UTC; business calculations adjusted to `Asia/Kolkata`.

---

## 3. Security Boundary & Data Flow

1. **Authentication:**
   - Client sends credentials (`phone_number`, `password`).
   - Server returns JWT Access Token (signed with HMAC-SHA256, 1-hour expiry) + Refresh Token (UUID in DB, 30-day expiry).
   - All subsequent requests include `Authorization: Bearer <access_token>`.

2. **Authorization:**
   - Spring Security `OncePerRequestFilter` inspects the Authorization header, validates signature and expiration, loads user authorities, and sets the `SecurityContext`.

3. **Database Security:**
   - All queries use Spring Data JPA or parameterized queries, eliminating SQL injection.
   - Database credentials and secrets injected strictly via environment variables (never in source code).
