# Feature & Architecture Guide — EventHub

> Complete feature manual, API endpoints, role permissions, and architectural flows for **EventHub**.

---

## 1. Feature Architecture Overview

```mermaid
flowchart TD
    User([User / Admin]) --> Auth[Authentication Layer (Spring Security)]
    Auth --> WebApp[Web Application Layer]
    
    subgraph Core System
        WebApp --> EventModule[Event Management & Search]
        WebApp --> RegModule[Registration & Self-Attendance]
        WebApp --> AdminModule[Admin Control Center]
    end
    
    RegModule -->|Pessimistic Lock| DB[(PostgreSQL Database)]
    EventModule -->|@EntityGraph| DB
    AdminModule --> DB
    
    RegModule -->|Async Confirmation| Resend[Resend Email API]
    AdminModule -->|Async Update Alert| Resend
    Scheduler[Email Reminder Scheduler] -->|Daily 9 AM| Resend
```

---

## 2. Core Feature Specifications

### 2.1 User Registration & Authentication
- **User Signup**: Validates name, unique email, password matching (min 8 characters), and assigns default `ROLE_USER`.
- **Form Login**: Authenticates against BCrypt-encoded passwords. Supports 1 active session per user.
- **Auto Welcome Email**: Asynchronously sends a welcome HTML email upon successful account registration.

### 2.2 Event Browsing, Status & Search
- **Start Date & End Date**: Each event tracks `eventDate` (start time) and `endDate` (end time).
- **Dynamic Event Status**: Automatically computes `@Transient` status (`UPCOMING`, `ONGOING`, `ENDED`) based on system clock.
- **Filterable Grid View**: Search events by keyword (title/description), category, location, and date range.
- **Visual Status Badges**: Displays color-coded pills (`UPCOMING` in blue, `ONGOING` with animated pulsing green dot, `ENDED` in slate).
- **PostgreSQL Safe Queries**: Handles null parameters smoothly using `CAST(:keyword AS String)`.
- **Performance Optimized**: Uses `@EntityGraph` to fetch event speakers without N+1 queries.
- **Event Detail Page**: Comprehensive view showing Start/End times, venue, location, capacity, remaining seats, assigned speakers, and status badge.

### 2.3 Event Registration & User Self-Attendance
- **Concurrency Safe**: Uses `PESSIMISTIC_WRITE` locking to guarantee zero overbooking even under simultaneous registrations.
- **Past Event Guard**: Prevents registrations for events that have passed or ended.
- **Atomic Counter Management**: Atomically increments `registrationCount` at the database query level.
- **Instant Email Confirmation**: Asynchronously triggers a branded HTML email confirmation to the user's inbox.
- **Live User Self-Attendance**: Registered users can click **Mark My Attendance** directly on the event detail page (`POST /events/{id}/attend`). This action is enabled **only while the event is ONGOING**.
- **Registration Cancellation**: Users can cancel registrations before an event ends, freeing up seats atomically.

### 2.4 Administrative Control Center (`ROLE_ADMIN`)
- **Dashboard**: High-level system statistics (Total Events, Active Registrations, Total Users, Total Speakers).
- **Event Management**: Create, edit, update, or delete events with start/end date validation.
- **Automated Update Notification**: Editing an event automatically calculates field-level changes and emails an amber-themed change summary to all registered users.
- **Speaker Management**: Full CRUD for event speakers (Name, Bio, Photo URL).
- **Role-Only User Management**: Admin view allows updating user roles (`ROLE_USER` ↔ `ROLE_ADMIN`) inline while keeping credentials and account state secure.
- **Attendance Tracking**: Admin panel displays check-in metrics and attendance status per event.

### 2.5 Automated Email Notifications
- **Welcome Email**: Sent upon new user account creation.
- **Registration Confirmation**: Sent immediately after successful event registration.
- **Event Update Alert**: Sent to all registered users whenever an admin updates event details.
- **Scheduled Reminder Job**: `@Scheduled(cron = "0 0 9 * * *")` checks daily at 9:00 AM for events scheduled within the next 20–28 hours and emails reminders automatically.

---

## 3. Endpoints & Permission Matrix

| Endpoint | Method | Role Required | Description |
|---|---|---|---|
| `/` | `GET` | Public | Homepage redirecting to event list |
| `/login` | `GET/POST` | Public | User authentication login page |
| `/register` | `GET/POST` | Public | User account registration |
| `/events` | `GET` | Public | Search and list events with status badges |
| `/events/{id}` | `GET` | Public | Detailed view of a single event |
| `/events/{id}/attend` | `POST` | `USER` / `ADMIN` | User self-marks attendance for ongoing event |
| `/registrations/register/{id}` | `POST` | `USER` / `ADMIN` | Register for an event |
| `/registrations/cancel/{id}` | `POST` | `USER` / `ADMIN` | Cancel an event registration |
| `/my-registrations` | `GET` | `USER` / `ADMIN` | User's registered events dashboard |
| `/admin/dashboard` | `GET` | `ADMIN` | Admin analytics dashboard |
| `/admin/events/**` | `GET/POST` | `ADMIN` | Admin Event CRUD operations |
| `/admin/speakers/**` | `GET/POST` | `ADMIN` | Admin Speaker CRUD operations |
| `/admin/users` | `GET` | `ADMIN` | View registered user roster |
| `/admin/users/{id}/role` | `POST` | `ADMIN` | Update user role only (`ROLE_USER` ↔ `ROLE_ADMIN`) |
| `/admin/attendance/{id}` | `GET/POST` | `ADMIN` | Track and mark event attendance |

---

## 4. UI Design System & Glassmorphism

- **Dark Mode Palette**: Primary background `#0F0F1A`, Container `#1A1A2E`, Surface Accent `#16213E`, Primary Accent `#6C63FF`.
- **Glassmorphism**: Semi-transparent card backdrops with backdrop filter blur (`backdrop-filter: blur(12px)`).
- **Status Pills**: Colored status badges (`UPCOMING`, `ONGOING` with pulse animation, `ENDED`).
- **Responsive Layout**: Mobile-first grid layouts built with CSS Flexbox & Grid.
