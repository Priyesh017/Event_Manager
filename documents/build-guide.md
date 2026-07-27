# Build Guide — EventHub (Event Management System)

> **Audience**: Developers building, maintaining, or auditing this application.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Technology Decisions](#technology-decisions)
3. [Build Stages & Architecture](#build-stages--architecture)
4. [Codebase Optimizations & Refactoring](#codebase-optimizations--refactoring)
5. [Running Locally](#running-locally)
6. [Environment Configuration](#environment-configuration)
7. [Testing & Verification](#testing--verification)

---

## Project Overview

EventHub is a full-stack web application for organizing, searching, and participating in events. It allows:
- **Users** to browse upcoming/ongoing/past events with dynamic status badges (`UPCOMING`, `ONGOING`, `ENDED`), view speaker details, search/filter events, register/cancel registrations, and mark their attendance live during ongoing events.
- **Admins** to manage events (CRUD with start/end date validation), manage speakers, handle user accounts with role-only updates (`ROLE_USER` ↔ `ROLE_ADMIN`), track attendance, and automatically notify registered users when event details change.
- **Automated Email System** powered by Resend API to send instant registration confirmation emails, event detail update notifications, welcome emails, and scheduled 24-hour event reminders.

Built with **Spring Boot 3.4.5 + Thymeleaf + PostgreSQL + Spring Security + Resend API**.

---

## Technology Decisions

| Component | Chosen | Why |
|---|---|---|
| Backend | Spring Boot 3.4.5 | Stable GA release; robust enterprise ecosystem |
| Template | Thymeleaf | Server-side rendering, seamless security integration, no JWT required |
| Database | Neon PostgreSQL | Cloud serverless database with pooler connection support |
| Migrations | Flyway | Version-controlled migrations (`V1__init_schema.sql`, `V2__add_end_date_to_events.sql`) |
| Security | Spring Security (Form Login) | Session-based authentication with BCrypt strength 12 |
| Environment | Dotenv Java | Safe local `.env` parsing with production fallbacks |
| Email | Resend Java SDK | Asynchronous, reliable HTML transactional email delivery |
| Testing | JUnit 5 + Mockito + H2 | Automated unit and integration testing suite |

---

## Build Stages & Architecture

### Stage 1 — Scaffolding & Dependencies
Configured canonical dependencies in `pom.xml`:
- `spring-boot-starter-web`, `spring-boot-starter-thymeleaf`, `spring-boot-starter-security`
- `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`
- `flyway-core`, `flyway-database-postgresql`, `dotenv-java`, `resend-java`

### Stage 2 — Database & Entities
Entities created with proper JPA mappings:
- `User` (role-based identity: `ROLE_USER`, `ROLE_ADMIN`)
- `Event` (title, description, `eventDate` [start], `endDate`, category, venue, capacity, registrationCount, `@Transient` `EventStatus`, ManyToMany with `Speaker`)
- `Speaker` (name, bio, photoUrl)
- `Registration` (User-Event relationship with unique DB constraint and `attended` boolean flag)

### Stage 3 — Security & Authentication
- Session-based security configured in `SecurityConfig.java`.
- Role-based routing: `/admin/**` restricted to `ROLE_ADMIN`.
- Dynamic database URI parsing in `EventManagementSystemApplication.java` extracts embedded credentials safely.

### Stage 4 — Concurrency, Event Lifecycle & Attendance
- `PESSIMISTIC_WRITE` locking applied in `EventRepository.findByIdWithLock()` during registration.
- Atomic SQL counter increments (`atomicIncrementCount`) prevent overbooking race conditions.
- Validation logic checks for past event signups and duplicate user registrations.
- `EventStatus` (`UPCOMING`, `ONGOING`, `ENDED`) computed dynamically.
- `AttendanceService.markSelfAttendance()` allows users to check in live during `ONGOING` events.

### Stage 5 — UI Layout & Glassmorphic Styling
- Built Thymeleaf layout templates (`layout/base.html`) featuring dark mode aesthetics, glassmorphism, status badges with animated pulse indicators, responsive navigation, and alert notification banners.

---

## Codebase Optimizations & Refactoring

1. **Clean Environment Configuration**: Removed `render.yaml` infrastructure artifact to maintain clean environment configuration management via standard environment variables and `Dockerfile`.
2. **Logged-in User Identity & Attendance Resolution**: Optimized `EventController.viewEvent()` by injecting `UserService` and `AttendanceService` to resolve user registration status and attendance status accurately.
3. **Domain Protection on Capacity & Dates**: Added validation in `EventServiceImpl.updateEvent()` preventing admins from lowering event capacity below current active registrations and requiring `endDate` to be after `eventDate`.
4. **Event Change Notification**: Implemented field-level change detection in `EventServiceImpl.updateEvent()` that automatically triggers asynchronous HTML email notifications to all registered users.
5. **Role-Only Admin User Update**: Constrained `AdminUserController` role updates to role-only modifications (`ROLE_USER` ↔ `ROLE_ADMIN`), safeguarding user account credentials.
6. **Admin Creation CLI Script**: Added `AdminCliRunner` and bash/powershell scripts to securely generate custom admins without hardcoding default passwords in the database or codebase.
7. **List Allocation Optimization**: Refactored `EventServiceImpl.resolveSpeakers()` to return immutable `List.of()` for empty ID lists, reducing heap allocation overhead.
8. **Pagination Bounding**: Bound page size parameters to `[1, 50]` range across public and admin controllers to protect against memory exhaustion.
9. **Graceful Exception Handling**: Added `IllegalArgumentException` and `IllegalStateException` handling in `GlobalExceptionHandler.java` for domain validation errors.

---

## Running Locally

### Prerequisites
- JDK 17+
- Maven 3.8+ (or included `mvnw`)
- PostgreSQL (or local H2 for testing)

### Setup Steps
1. Clone the repository and navigate to root:
   ```bash
   cd event-management-system
   ```
2. Create `.env` in the project root:
   ```env
   DATABASE_URL=jdbc:postgresql://localhost:5432/eventdb
   DB_USERNAME=postgres
   DB_PASSWORD=postgres
   RESEND_API_KEY=re_123456789
   RESEND_FROM_EMAIL=onboarding@resend.dev
   RESEND_FROM_NAME=EventHub
   APP_BASE_URL=http://localhost:8080
   ```
3. Create an initial Admin user using the CLI script:
   ```bash
   ./scripts/add-admin.sh "admin@eventhub.com" "SecurePassword123!"
   # On Windows: .\scripts\add-admin.ps1 -Email "admin@eventhub.com" -Password "SecurePassword123!"
   ```
4. Run Flyway migrations and start the server:
   ```bash
   ./mvnw spring-boot:run
   ```
5. Access application at `http://localhost:8080`.
   - Admin Login: Use the credentials you created in step 3.

---

## Environment Configuration

| Variable | Description | Default / Example |
|---|---|---|
| `DATABASE_URL` | PostgreSQL connection string | `jdbc:postgresql://host:5432/db?sslmode=require` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `secret` |
| `RESEND_API_KEY` | Resend API key for emails | `re_xxx` |
| `RESEND_FROM_EMAIL` | Sender email address | `noreply@domain.com` |
| `RESEND_FROM_NAME` | Sender display name | `EventHub` |
| `APP_BASE_URL` | Application root URL | `http://localhost:8080` |

---

## Testing & Verification

Run the full automated test suite (including controller, service, repository, and security tests):

```bash
./mvnw test -Dspring.profiles.active=test
```

Result: **17 / 17 tests passed (`BUILD SUCCESS`)**.
