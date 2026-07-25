# AI Coding Agent Specification & Efficiency Guidelines (`agent.md`)

> **System Target**: Event Management System (EventHub)
> **Stack**: Spring Boot 3.4.5 + Thymeleaf + PostgreSQL + Spring Security + Resend API

---

## 1. Operating Principles & Efficiency Directives

The AI Agent operating within this project must strictly adhere to the following workflow principles to guarantee fast, safe, and bug-free execution:

### 1.1 Never Guess, Always Verify (Authoritative Source Inspection)
- **Rule**: Never assume API signatures, DTO structures, database schemas, or file locations.
- **Action**: Always inspect files using `view_file` or `grep_search` before modifying imports, entity associations, or service method signatures.

### 1.2 Empirical Runtime Verification
- **Rule**: Creating or editing code is **not** job completion.
- **Action**: Every code modification MUST be followed by running automated tests via `./mvnw test -Dspring.profiles.active=test`. Never declare success without a clean `BUILD SUCCESS`.

### 1.3 Silent Log Extraction & Diagnosis First
- **Rule**: When errors occur during compilation, testing, or startup, read full, un-truncated logs.
- **Action**: Form diagnostic hypotheses strictly based on log tracebacks. Do not mask symptoms with empty catch blocks, fallbacks, or disabled assertions.

### 1.4 Preserving Codebase Integrity & Documentation Sync
- **Rule**: Whenever changes are made to source code, all corresponding test mocks, DTO validations, and documentation (`.agent/context.md`, `documents/*`, `code_audit_report.md`) must be synchronized immediately.

---

## 2. Technical Standards & Domain Protocols

### 2.1 Concurrency & Data Consistency
- **Pessimistic Locking**: Concurrent state modifications (such as event capacity registration checks) MUST use `@Lock(LockModeType.PESSIMISTIC_WRITE)` to prevent race conditions.
- **Atomic Operations**: Perform count increments/decrements at the database level (`UPDATE Event e SET e.registrationCount = e.registrationCount + 1 WHERE ...`) rather than read-modify-write in Java memory.
- **Database Constraints**: Handle DB unique constraint violations (`DataIntegrityViolationException`) gracefully in `GlobalExceptionHandler` with clear user feedback.

### 2.2 PostgreSQL Type Safety & JPQL Compliance
- **String Casting**: When using JPQL `LOWER()` or `LIKE` with nullable parameters on PostgreSQL, explicitly cast using `CAST(:param AS String)` to avoid `ERROR: function lower(bytea) does not exist`.
- **Timestamp Casting**: Always cast optional date range filter parameters using `CAST(:dateFrom AS timestamp)`.

### 2.3 Environment Variable & Configuration Resilience
- **Dotenv Integration**: Environment variables must be loaded dynamically using `Dotenv.configure().ignoreIfMissing().ignoreIfMalformed().load()`.
- **Database Connection Parsing**: Dynamically parse `DATABASE_URL` strings containing inline credentials (`jdbc:postgresql://user:pass@host/db`) into standalone `spring.datasource.username`, `spring.datasource.password`, and sanitized `spring.datasource.url` properties.

### 2.4 Performance & Query Optimization
- **N+1 Prevention**: Ensure child collections (e.g., `Event.speakers`) are eagerly fetched using `@EntityGraph` in repository search queries to minimize round-trips.
- **Validation Bounds**: Enforce input payload limits (`@Size(max = 5000)`) on rich text inputs (`EventDto.description`, `SpeakerDto.bio`).

### 2.5 Event Lifecycle, Status & Attendance Tracking
- **Start & End Dates**: Events store `eventDate` (start date/time) and `endDate` (end date/time). `endDate` must strictly be after `eventDate`.
- **Computed EventStatus**: Dynamic `@Transient` calculation (`UPCOMING`, `ONGOING`, `ENDED`) derived from current time relative to start/end dates.
- **User Self-Attendance Check-In**: Users registered for an event can mark their attendance via `POST /events/{id}/attend`. Check-in is strictly allowed **only while the event status is ONGOING**.

### 2.6 Event Change Notification Protocol
- **Automated Registrant Email Alerts**: When an admin updates an existing event in `EventServiceImpl.updateEvent()`, the system compares previous and new values across title, description, category, dates, venue, and location.
- **Asynchronous Mail Dispatch**: If changes are detected, `EmailService.sendEventUpdateNotification()` sends an amber-themed HTML notification detailing exact modifications to all registered users without blocking the main update thread.

### 2.7 Restricted Admin User Management
- **Role-Only Updates**: Admin user management (`POST /admin/users/{id}/role`) strictly limits modifications to toggling the user role (`ROLE_USER` ↔ `ROLE_ADMIN`). User credentials, email, and security properties are protected from arbitrary admin edits.

---

## 3. Communication & Output Rules

- **Markdown Links**: Always link to relevant files using standard markdown with `file:///` URLs (e.g., `[EventServiceImpl.java](file:///e:/Guvi%20Projects/EventManagementSystem/event-management-system/src/main/java/in/guvi/event/management/system/service/impl/EventServiceImpl.java)`).
- **Concise Summaries**: State changes clearly without re-printing raw source code snippets that the user can inspect in the file diffs.
- **Task Tracking**: Keep `task.md` and `walkthrough.md` updated after every milestone execution.
