# 🎯 EventHub — Event Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue.svg)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Migrations-Flyway-red.svg)](https://flywaydb.org/)
[![Thymeleaf](https://img.shields.io/badge/Template-Thymeleaf-005F0F.svg)](https://www.thymeleaf.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> A modern, full-stack event management and registration platform built with **Spring Boot 3.4.5**, **Thymeleaf**, **PostgreSQL**, **Spring Security**, and the **Resend Email API**. Features concurrency-safe seat reservation, dynamic event status tracking, live user self-attendance check-in, automated event update notifications, and a responsive glassmorphic dark-mode interface.

---

## 📋 Table of Contents
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [System Architecture](#-system-architecture)
- [Database Schema & Migrations](#-database-schema--migrations)
- [Getting Started](#-getting-started)
- [Environment Configuration](#-environment-configuration)
- [Automated Email Workflow](#-automated-email-workflow)
- [Testing & Quality Assurance](#-testing--quality-assurance)
- [Security & GitGuardian Compliance](#-security--gitguardian-compliance)
- [Project Directory Structure](#-project-directory-structure)
- [License](#-license)

---

## ✨ Features

### 👤 User Features
- **Event Discovery & Filtering**: Search events by keyword, category (Conference, Workshop, Webinar, Meetup), city/location, and date range.
- **Dynamic Event Status**: Live status badge (`UPCOMING` 🕐, `ONGOING` 🟢 with animated pulse dot, `ENDED` 🔴) calculated dynamically from system time relative to event start and end timestamps.
- **Concurrency-Safe Registration**: Instant seat booking protected by database-level pessimistic write locking and atomic DB increment queries to prevent overbooking under high traffic.
- **Live Self-Attendance Check-In**: Registered attendees can mark themselves present via **Mark My Attendance** directly on the event detail page—available **only while the event status is ONGOING**.
- **Self-Service Cancellation**: Registered users can cancel signups prior to event completion, releasing seats atomically.
- **My Registrations Dashboard**: Personal portal to track all upcoming and past event registrations.

### 🛡️ Admin Features (`ROLE_ADMIN`)
- **Control Center Dashboard**: Analytics overview displaying total events, active registrations, user count, and speaker metrics.
- **Event Lifecycle CRUD**: Create, edit, and delete events with start date, end date, venue, location, and capacity management.
- **Automated Registrant Change Alerts**: Editing event details automatically evaluates field-level changes and dispatches an HTML notification email to all registered users detailing exact updates.
- **Speaker Management**: Dedicated CRUD panel to manage speaker profiles, bios, and headshot assets.
- **Role-Only User Roster Management**: Secure user management allowing admins to toggle user roles (`ROLE_USER` ↔ `ROLE_ADMIN`) while protecting credentials and security attributes.

---

## 🛠 Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| **Framework** | Spring Boot 3.4.5 (GA) | Enterprise Java backend framework |
| **View Engine** | Thymeleaf + HTML5/CSS3 | Server-side HTML rendering with Security Dialect |
| **Database** | Neon PostgreSQL | Cloud serverless relational database |
| **Schema Migrations** | Flyway | Versioned database migrations (`V1`, `V2`) |
| **Security** | Spring Security | Session-based form authentication & BCrypt password hashing |
| **Email Service** | Resend Java SDK | Asynchronous transactional HTML email delivery |
| **Configuration** | Dotenv Java | Dynamic environment variable loader for zero-secret code |
| **Testing** | JUnit 5, Mockito, H2 DB | Unit testing and integration test execution |

---

## 🏗 System Architecture

```mermaid
flowchart TD
    Client([User / Admin Browser]) --> Auth[Spring Security Layer]
    Auth --> Controllers[Spring MVC Controllers]

    subgraph Core Business Layer
        Controllers --> EventService[Event Service]
        Controllers --> RegService[Registration Service]
        Controllers --> AttendService[Attendance Service]
        Controllers --> UserService[User Service]
    end

    subgraph Data & Async Layer
        RegService -->|Pessimistic Lock| DB[(PostgreSQL Database)]
        EventService -->|@EntityGraph Eager Fetch| DB
        RegService -->|Async Confirmation| EmailService[Email Service]
        EventService -->|Async Change Alert| EmailService
        Scheduler[Daily 9 AM Cron] -->|Reminder Job| EmailService
        EmailService -->|REST API| Resend[Resend Email Gateway]
    end
```

---

## 🗄 Database Schema & Migrations

Database schema versioning is enforced via **Flyway**:

1. **`V1__init_schema.sql`**: Initial baseline schema:
   - `users`: Core identity table with unique email, BCrypt password hash, role (`ROLE_USER` / `ROLE_ADMIN`), enabled state.
   - `events`: Event catalog storing title, description, category, `event_date` (start date), venue, location, capacity, `registration_count`.
   - `speakers`: Speaker profile information.
   - `event_speakers`: Join table establishing Many-to-Many relationship between events and speakers.
   - `registrations`: Registrations table linking users and events with unique constraint `(user_id, event_id)` and check-in flag `attended`.

2. **`V2__add_end_date_to_events.sql`**: Event duration extension:
   - Adds non-null `end_date` column to `events` table with index `idx_events_end_date`.
   - Automatically backfills existing event records with `end_date = event_date + 2 hours`.

---

## 🚀 Getting Started

### Prerequisites
- **JDK 17** or higher
- **Maven 3.8+** (or use the repository `./mvnw` wrapper)
- **PostgreSQL** database instance (or local PostgreSQL)

### Step 1: Clone the Repository
```bash
git clone https://github.com/Priyesh017/Event_Manager.git
cd event-management-system
```

### Step 2: Configure Environment Variables
Copy `.env.example` to `.env` in the project root:
```bash
cp .env.example .env
```

Configure your local `.env` parameters:
```env
SERVER_PORT=8080
DATABASE_URL=jdbc:postgresql://localhost:5432/eventdb
RESEND_API_KEY=your_resend_api_key_here
RESEND_DOMAIN=yourdomain.com
APP_ADMIN_EMAIL=admin@eventhub.com
APP_ADMIN_PASSWORD=your_secure_admin_password_here
```

### Step 3: Create an Admin Account
Use the included CLI script to generate a secure administrator account without hardcoding passwords:
```bash
./scripts/add-admin.sh "admin@eventhub.com" "YourSecurePassword123!"
```
*(Windows users: Run `.\scripts\add-admin.ps1 -Email "admin@eventhub.com" -Password "YourSecurePassword123!"` in PowerShell)*

### Step 4: Build & Run the Application
Execute Flyway migrations and launch the dev server:
```bash
./mvnw spring-boot:run
```

Access the web interface at `http://localhost:8080`.
Log in with the admin credentials you created in Step 3.

---

## ⚙ Environment Configuration

| Variable Name | Required | Default Value | Description |
|---|---|---|---|
| `SERVER_PORT` | No | `8080` | HTTP port for the Spring Boot application |
| `DATABASE_URL` | Yes | - | PostgreSQL JDBC URL string |
| `RESEND_API_KEY` | Yes | - | Transactional email API key from Resend |
| `RESEND_DOMAIN` | No | `securevault.co.in` | Sender domain configured in Resend |
| `APP_ADMIN_EMAIL` | No | `admin@eventhub.com` | (Legacy) Default admin email for env seeding |
| `APP_ADMIN_PASSWORD` | No | (Disabled if blank) | (Legacy) Default admin password for env seeding |
| `APP_BASE_URL` | No | `http://localhost:8080` | Root URL used in email link generators |

---

## 📧 Automated Email Workflow

EventHub integrates asynchronous transactional emails using the **Resend SDK**:

- 📩 **Welcome Email**: Sent automatically upon successful user account registration.
- 🎟️ **Registration Confirmation**: HTML ticket receipt emailed immediately upon event signup.
- 📝 **Event Detail Change Alert**: When an admin modifies event properties (title, description, category, dates, venue, location), an amber-themed update log email is sent to all registered participants.
- ⏰ **Automated 24-Hour Reminder**: A scheduled background job (`@Scheduled(cron = "0 0 9 * * *")`) checks daily at 9:00 AM for events occurring within 24 hours and dispatches reminders automatically.

---

## 🧪 Testing & Quality Assurance

Run the automated test suite (includes repository, service logic, controller slices, and application context tests):

```bash
./mvnw test "-Dspring.profiles.active=test"
```

### Test Suite Summary
- **Total Tests**: 17
- **Failures**: 0
- **Errors**: 0
- **Result**: `BUILD SUCCESS` ✅

---

## 🔐 Security & GitGuardian Compliance

To prevent credential leaks and ensure enterprise security compliance:
- 🚫 **No Hardcoded Passwords or API Keys**: All secrets are loaded dynamically from environment variables via `dotenv-java`.
- 🔑 **Secure Admin CLI**: Admin accounts are created using a dedicated script (`AdminCliRunner`) that hashes passwords via BCrypt and shuts down the JVM safely without booting the web server.
- 🛡️ **Role Protection**: Admin user management restricts edits exclusively to toggling roles (`ROLE_USER` ↔ `ROLE_ADMIN`), ensuring user passwords and sensitive fields cannot be modified via admin forms.
- 🔒 **CSRF & XSS Shield**: CSRF protection enabled across all POST/PUT/DELETE forms with HTML attribute escaping in Thymeleaf templates.

---

## 📁 Project Directory Structure

```
event-management-system/
├── .agent/                    # AI Agent specification and mistake tracking context
│   ├── agent.md               # Agent operational & efficiency guidelines
│   └── context.md             # Developer context & mistake tracker
├── documents/                 # Complete technical documentation suite
│   ├── admin-access-guide.md  # CLI instructions for admin creation
│   ├── build-guide.md         # Comprehensive build architecture guide
│   ├── credentials.md         # Admin credentials & access configuration
│   ├── feature-guide.md       # Feature manual & API permission matrix
│   └── workflow.md            # Development & QA testing workflow
├── scripts/                   # System automation scripts
│   ├── add-admin.sh           # Bash admin creation CLI script
│   └── add-admin.ps1          # PowerShell admin creation CLI script
├── src/
│   ├── main/
│   │   ├── java/in/guvi/event/management/system/
│   │   │   ├── config/        # Security, UserDetails, App beans
│   │   │   ├── controller/    # Public & Admin web controllers
│   │   │   ├── dto/           # Data Transfer Objects with validation
│   │   │   ├── entity/        # JPA Entities (User, Event, Speaker, Registration)
│   │   │   ├── enums/         # EventCategory, EventStatus, Role
│   │   │   ├── exception/     # Global exception handlers
│   │   │   ├── repository/    # JPA Repositories with pessimistic locks & JPQL
│   │   │   ├── service/       # Business logic interfaces & implementations
│   │   │   └── util/          # DataInitializer seed component
│   │   └── resources/
│   │       ├── db/migration/  # Flyway schema scripts (V1, V2)
│   │       ├── templates/     # Thymeleaf HTML views (events, admin, auth)
│   │       └── application.yaml
│   └── test/                  # JUnit 5 & Mockito test suite
├── .env.example               # Safe environment configuration template
├── Dockerfile                 # Container deployment specification
├── pom.xml                    # Maven build configuration
└── README.md                  # Project overview (this file)
```

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.
