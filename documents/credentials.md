# Admin & Access Credentials — EventHub

> **Security Note**: This document outlines default local development and testing credentials. In production environments, credentials **must** be managed exclusively via environment variables and cloud key management services.

---

## 🔑 Generating Admin Account Credentials

EventHub does not come with hardcoded default admin credentials. To manage users and events securely, you must generate an admin account using the provided Command Line Interface (CLI) scripts.

For detailed instructions, refer to the **[Admin Access Guide](file:///e:/Guvi%20Projects/EventManagementSystem/event-management-system/documents/admin-access-guide.md)**.

### Quick Start
Use the `add-admin` script to securely create an administrator in the database.

**Windows:**
```powershell
.\scripts\add-admin.ps1 -Email "admin@eventhub.com" -Password "YourSecurePassword123!"
```

**Linux/macOS:**
```bash
./scripts/add-admin.sh "admin@eventhub.com" "YourSecurePassword123!"
```

*These scripts safely initialize the Spring Boot context without starting the web server, hash your password with BCrypt, and shut down.*

| Property | Description |
|---|---|
| **Role** | `ROLE_ADMIN` (Full access to Admin Panel `/admin/**`) |
| **Email** | Provided during script execution |
| **Password** | Provided during script execution |
| **Login Portal** | `http://localhost:8080/login` |

---

## 👤 Admin Role Permissions & Access Matrix

An account with `ROLE_ADMIN` has access to the following administrative features:

- **📊 Admin Dashboard (`/admin/dashboard`)**: View system-wide metrics (total events, active registrations, user count, speaker metrics).
- **📅 Event Management (`/admin/events`)**: Create, edit, and delete events, set start/end timestamps, assign speakers, and auto-notify registrants upon details update.
- **🎤 Speaker Management (`/admin/speakers`)**: Full CRUD operations for managing event speakers, bios, and profile images.
- **👥 User Roster Management (`/admin/users`)**: View registered user roster and perform role-only updates (`ROLE_USER` ↔ `ROLE_ADMIN`).
- **📋 Attendance Tracking (`/admin/attendance/{id}`)**: Track and mark attendee check-ins per event.

---

## ⚙ Fallback: Environment Variable Seeding

While the CLI scripts are the recommended way to create admins, you can also set the following environment variables in `.env` to automatically seed an admin upon application startup:

```env
# Admin Seeding Credentials (Legacy/CI)
APP_ADMIN_EMAIL=admin@eventhub.com
APP_ADMIN_PASSWORD=your_secure_admin_password_here
```

> **Note**: If `APP_ADMIN_PASSWORD` is left empty or not specified, automatic admin account seeding will be safely skipped to prevent unauthorized default access.

---

## 🛡️ Production Security Checklist

1. Change `APP_ADMIN_PASSWORD` before deploying to staging or production environments.
2. Store `APP_ADMIN_PASSWORD` in your hosting provider's secret manager (e.g., Render Environment Variables, AWS Secrets Manager, GitHub Secrets).
3. Do not commit `.env` containing active passwords to public Git repositories.
