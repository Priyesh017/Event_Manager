# Workflow — Event Management System

## Complete System Workflow

---

## 1. User Registration Flow

```
User visits /register
    → Fills name, email, password
    → POST /register
    → UserService.registerUser()
        → Validate email uniqueness
        → Encode password (BCrypt)
        → Save user (ROLE_USER)
    → EmailService.sendWelcomeEmail()
    → Redirect to /login?registered=true
```

## 2. Authentication Flow

```
User visits /login
    → Fills email + password
    → Spring Security processes form
        → CustomUserDetailsService.loadUserByUsername(email)
        → BCryptPasswordEncoder verifies password
        → Session created
    → Redirect to / (home)
```

## 3. Event Browsing Flow

```
User visits /events
    → EventController.listEvents(searchDto, pageable)
        → EventService.searchEvents(category, location, date, keyword)
        → Returns Page<Event>
    → Thymeleaf renders event cards
    → User clicks event → /events/{id}
        → EventController.viewEvent(id)
        → Shows: title, date, venue, description, speakers, available seats
```

## 4. Event Registration Flow

```
Authenticated user visits /events/{id}
    → Clicks "Register"
    → POST /registrations/register/{eventId}
    → RegistrationController → RegistrationService.register(userId, eventId)
        → Check: already registered? → throw DuplicateRegistrationException
        → Check: event at capacity? → throw EventFullException
        → Save Registration entity
        → Increment event.registrationCount
    → EmailService.sendRegistrationConfirmation(user, event)
    → Redirect to /my-registrations
```

## 5. Admin: Create Event Flow

```
Admin visits /admin/events/create
    → Fills form: title, description, date, venue, capacity, category, speakers
    → POST /admin/events/create
    → AdminEventController → EventService.createEvent(eventDto, adminUser)
        → Save Event entity
        → Link selected speakers (ManyToMany)
    → Redirect to /admin/events
```

## 6. Admin: Mark Attendance Flow

```
Admin visits /admin/attendance/{eventId}
    → Sees list of registered users
    → Checks checkboxes for attended users
    → POST /admin/attendance/{eventId}/mark
    → AdminAttendanceController → AttendanceService.markAttendance(eventId, userIds)
        → Update registration.attended = true for checked users
    → Redirect back with success message
```

## 7. Email Reminder Flow (Scheduled)

```
@Scheduled(cron = "0 9 * * * *") — Every day at 9 AM
    → EmailReminderScheduler.sendReminders()
    → EventService.getEventsTomorrow()
        → Find events where event_date BETWEEN now+20h AND now+28h
    → For each event:
        → RegistrationService.getRegistrationsForEvent(event)
        → For each registration:
            → EmailService.sendEventReminder(user, event)
```

## 8. Search & Filter Flow

```
GET /events?category=WORKSHOP&location=Chennai&date=2026-08-01&keyword=java

EventController.listEvents(EventSearchDto):
    → EventService.searchEvents(dto)
        → JPA Specification or JPQL query:
            WHERE (:category IS NULL OR e.category = :category)
            AND (:location IS NULL OR e.location LIKE %:location%)
            AND (:date IS NULL OR DATE(e.event_date) = :date)
            AND (:keyword IS NULL OR e.title LIKE %:keyword%)
    → Returns paginated results
    → Thymeleaf re-renders with filters applied
```

## Role-Based Access Matrix

| URL Pattern         | ROLE_USER | ROLE_ADMIN |
|---------------------|-----------|------------|
| /                   | ✅         | ✅          |
| /events/**          | ✅         | ✅          |
| /my-registrations   | ✅         | ✅          |
| /registrations/**   | ✅         | ✅          |
| /admin/**           | ❌         | ✅          |
| /login, /register   | PUBLIC    | PUBLIC     |
