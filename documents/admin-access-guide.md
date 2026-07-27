# Admin Access Guide

This document explains how to safely generate custom admin access for the Event Management System without hardcoding credentials in the codebase.

## Overview

The application is designed to be secure by default. There are no hardcoded default admin credentials in the database or the code. To manage users and events, you will need an admin account. 

You can create an admin account using the provided Command Line Interface (CLI) scripts.

## Prerequisites

- Java 21 is installed.
- The PostgreSQL database is running and the credentials in `.env` are correct.
- You are in the root directory of the project.

## Creating an Admin Account

We provide scripts for both Windows (PowerShell) and Unix-like (Bash) environments.

### Windows (PowerShell)

Open PowerShell and run the `add-admin.ps1` script from the `scripts` directory:

```powershell
.\scripts\add-admin.ps1 -Email "admin@yourdomain.com" -Password "SecurePassword123!"
```

### Linux / macOS (Bash)

Make sure the script is executable (if it isn't already):

```bash
chmod +x scripts/add-admin.sh
```

Run the `add-admin.sh` script from the project root:

```bash
./scripts/add-admin.sh "admin@yourdomain.com" "SecurePassword123!"
```

## How it works

These scripts run the Spring Boot application using a special CLI command: `--add-admin=email:password`. 

1. It connects to your database using the settings in your `.env` or `application.yaml` file.
2. It securely encrypts the provided password using BCrypt.
3. It inserts the new user directly into the `users` table with the `ROLE_ADMIN` role.
4. It shuts down immediately without starting the web server.

> [!NOTE]
> Because it uses `--spring.main.web-application-type=none`, it will not conflict with a running instance of the application on port 8080. You can run this script even while the main server is running.

## Troubleshooting

- **Database Connection Error**: Ensure your `.env` file is properly configured with `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`.
- **Command Not Found**: Ensure you are running the script from the root of the project, so it can locate the `./mvnw` or `.\mvnw.cmd` wrapper.
