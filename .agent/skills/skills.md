# Skills — Event Management System

## skill/auth
**Purpose**: Handle Spring Security configuration and user authentication
**Files**:
- `SecurityConfig.java` — form login, role-based URL rules, BCrypt
- `CustomUserDetailsService.java` — load user from DB
**Key Rules**:
- Use `BCryptPasswordEncoder` always
- Enable CSRF for all state-changing operations
- Session timeout: 30 minutes

---

## skill/event
**Purpose**: Full event lifecycle management
**Files**:
- `EventService.java / EventServiceImpl.java`
- `EventController.java`
- `AdminEventController.java`
**Key Rules**:
- Use `Page<Event>` for all listings (pagination)
- Search with JPA Specifications
- Category stored as VARCHAR (EventCategory enum)

---

## skill/email
**Purpose**: Send transactional emails via Resend API
**Files**:
- `EmailService.java / EmailServiceImpl.java`
- `EmailReminderScheduler.java`
- `templates/email/confirmation.html`
- `templates/email/reminder.html`
**Key Rules**:
- Always use HTML email templates, not plain text
- Never block the main thread for email sending (use `@Async`)
- Log failures, never throw exceptions that break the registration flow

---

## skill/admin
**Purpose**: Admin panel for managing events, speakers, users, attendance
**Files**:
- `AdminDashboardController.java`
- `AdminEventController.java`
- `AdminSpeakerController.java`
- `AdminAttendanceController.java`
**Key Rules**:
- All admin routes protected by `ROLE_ADMIN`
- Redirect-after-POST pattern to prevent duplicate submissions
- Flash messages for success/error feedback

---

## skill/testing
**Purpose**: Write and run tests
**Layers**:
- Service: `@ExtendWith(MockitoExtension.class)` — mock repositories
- Controller: `@WebMvcTest` + `MockMvc` — mock services
- Repository: `@DataJpaTest` + H2 — real DB queries
- E2E: `@SpringBootTest(webEnvironment = RANDOM_PORT)` + H2
