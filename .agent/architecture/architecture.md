# System Architecture — Event Management System

## Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT BROWSER                           │
│                    (Thymeleaf HTML Pages)                       │
└─────────────────────────┬───────────────────────────────────────┘
                          │ HTTP (Session Cookie)
┌─────────────────────────▼───────────────────────────────────────┐
│                   SPRING BOOT APPLICATION                       │
│   ┌─────────────┐  ┌──────────────┐  ┌─────────────────────┐   │
│   │Spring MVC   │  │Spring        │  │Thymeleaf Template   │   │
│   │Controllers  │  │Security      │  │Engine               │   │
│   └──────┬──────┘  └──────────────┘  └─────────────────────┘   │
│          │                                                       │
│   ┌──────▼──────────────────────────────────────────────────┐   │
│   │                   SERVICE LAYER                         │   │
│   │  UserSvc | EventSvc | RegistrationSvc | EmailSvc        │   │
│   │  SpeakerSvc | AttendanceSvc                             │   │
│   └──────┬──────────────────────────────────────────────────┘   │
│          │                                                       │
│   ┌──────▼──────────────────────────────────────────────────┐   │
│   │                  REPOSITORY LAYER                       │   │
│   │  Spring Data JPA Repositories (CRUD + custom queries)   │   │
│   └──────┬──────────────────────────────────────────────────┘   │
└──────────┼──────────────────────────────────────────────────────┘
           │ JDBC/SSL
┌──────────▼──────────────────────────────────────────────────────┐
│              NEON POSTGRESQL (Cloud Serverless)                 │
│   Tables: users, events, speakers, registrations,               │
│           event_speakers, event_categories                      │
└─────────────────────────────────────────────────────────────────┘
           │
┌──────────▼──────────────────────────────────────────────────────┐
│              RESEND (Email API)                                 │
│   - Registration Confirmation                                   │
│   - Event Reminders (Scheduled)                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Package Structure

```
in.guvi.event.management.system
├── config/
│   ├── SecurityConfig.java          ← Spring Security configuration
│   ├── WebMvcConfig.java            ← MVC formatters, static resources
│   └── ResendConfig.java            ← Resend client bean
├── controller/
│   ├── HomeController.java          ← Landing page
│   ├── AuthController.java          ← Login / Register
│   ├── EventController.java         ← Browse / Detail / Search
│   ├── RegistrationController.java  ← Register for event, My events
│   └── admin/
│       ├── AdminDashboardController.java
│       ├── AdminEventController.java
│       ├── AdminSpeakerController.java
│       ├── AdminUserController.java
│       └── AdminAttendanceController.java
├── dto/
│   ├── UserRegistrationDto.java
│   ├── EventDto.java
│   ├── EventSearchDto.java
│   ├── SpeakerDto.java
│   └── RegistrationDto.java
├── entity/
│   ├── User.java
│   ├── Event.java
│   ├── Speaker.java
│   └── Registration.java
├── enums/
│   ├── Role.java
│   └── EventCategory.java
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── DuplicateRegistrationException.java
│   ├── EventFullException.java
│   └── GlobalExceptionHandler.java
├── repository/
│   ├── UserRepository.java
│   ├── EventRepository.java
│   ├── SpeakerRepository.java
│   └── RegistrationRepository.java
├── service/
│   ├── UserService.java
│   ├── EventService.java
│   ├── SpeakerService.java
│   ├── RegistrationService.java
│   ├── EmailService.java
│   ├── AttendanceService.java
│   └── impl/
│       ├── UserServiceImpl.java
│       ├── EventServiceImpl.java
│       ├── SpeakerServiceImpl.java
│       ├── RegistrationServiceImpl.java
│       └── EmailServiceImpl.java
├── scheduler/
│   └── EmailReminderScheduler.java
├── util/
│   └── DataInitializer.java
└── EventManagementSystemApplication.java
```

## Database Schema

```
users
├── id (PK, BIGSERIAL)
├── name (VARCHAR 100)
├── email (VARCHAR 150, UNIQUE)
├── password (VARCHAR 255)
├── role (VARCHAR 20) ← ROLE_USER | ROLE_ADMIN
├── enabled (BOOLEAN)
└── created_at (TIMESTAMP)

events
├── id (PK, BIGSERIAL)
├── title (VARCHAR 200)
├── description (TEXT)
├── category (VARCHAR 50)
├── event_date (TIMESTAMP)
├── venue (VARCHAR 200)
├── location (VARCHAR 200)
├── capacity (INTEGER)
├── registration_count (INTEGER)
├── image_url (VARCHAR 500)
├── created_by (FK → users.id)
└── created_at (TIMESTAMP)

speakers
├── id (PK, BIGSERIAL)
├── name (VARCHAR 100)
├── bio (TEXT)
└── photo_url (VARCHAR 500)

event_speakers (JOIN TABLE)
├── event_id (FK → events.id)
└── speaker_id (FK → speakers.id)

registrations
├── id (PK, BIGSERIAL)
├── user_id (FK → users.id)
├── event_id (FK → events.id)
├── registered_at (TIMESTAMP)
└── attended (BOOLEAN)
```

## Security Flow

```
Request → Spring Security Filter Chain
         → Session cookie present? → Load UserDetails from DB
         → Role check (ROLE_USER / ROLE_ADMIN)
         → /admin/** requires ROLE_ADMIN
         → /register, /login → PUBLIC
         → Everything else → AUTHENTICATED
```

## Tech Stack

| Layer       | Technology                        |
|-------------|-----------------------------------|
| Backend     | Spring Boot 3.4.5, Java 21        |
| Web MVC     | Spring MVC + Thymeleaf            |
| Security    | Spring Security (session/form)    |
| Database    | PostgreSQL (Neon serverless)      |
| ORM         | Spring Data JPA + Hibernate       |
| Migrations  | Flyway                            |
| Email       | Resend Java SDK                   |
| Testing     | JUnit 5, Mockito, H2              |
| Build       | Maven                             |
| Deployment  | Render / Railway                  |
