# Admin & Access Credentials — EventHub

> **Security Note**: This document outlines default local development and testing credentials. In production environments, credentials **must** be managed exclusively via environment variables and cloud key management services.

---

## 🔑 Default Admin Account Credentials

The application includes an automated data seeder (`DataInitializer`) that creates a default administrator account upon initial startup if specified in the configuration.

| Property | Default Value / Environment Variable | Description |
|---|---|---|
| **Role** | `ROLE_ADMIN` | Full access to Admin Panel (`/admin/**`) |
| **Email** | `admin@eventhub.com` (`APP_ADMIN_EMAIL`) | Administrator login username |
| **Password** | Configured via `APP_ADMIN_PASSWORD` (e.g. `Admin@123` in `.env`) | Administrator account password |
| **Login Portal** | `http://localhost:8080/login` | Form login URL |

---

## 👤 Admin Role Permissions & Access Matrix

An account with `ROLE_ADMIN` has access to the following administrative features:

- **📊 Admin Dashboard (`/admin/dashboard`)**: View system-wide metrics (total events, active registrations, user count, speaker metrics).
- **📅 Event Management (`/admin/events`)**: Create, edit, and delete events, set start/end timestamps, assign speakers, and auto-notify registrants upon details update.
- **🎤 Speaker Management (`/admin/speakers`)**: Full CRUD operations for managing event speakers, bios, and profile images.
- **👥 User Roster Management (`/admin/users`)**: View registered user roster and perform role-only updates (`ROLE_USER` ↔ `ROLE_ADMIN`).
- **📋 Attendance Tracking (`/admin/attendance/{id}`)**: Track and mark attendee check-ins per event.

---

## ⚙ How to Configure Admin Credentials in `.env`

To set or change the admin credentials for your local environment, add or modify the following lines in your `.env` file:

```env
# Admin Seeding Credentials
APP_ADMIN_EMAIL=admin@eventhub.com
APP_ADMIN_PASSWORD=your_secure_admin_password_here
```

> **Note**: If `APP_ADMIN_PASSWORD` is left empty or not specified, automatic admin account seeding will be safely skipped to prevent unauthorized default access.

---

## 🛡️ Production Security Checklist

1. Change `APP_ADMIN_PASSWORD` before deploying to staging or production environments.
2. Store `APP_ADMIN_PASSWORD` in your hosting provider's secret manager (e.g., Render Environment Variables, AWS Secrets Manager, GitHub Secrets).
3. Do not commit `.env` containing active passwords to public Git repositories.
