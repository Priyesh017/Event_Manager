# Agent Rules — Event Management System

## Core Development Rules

### R1 — No Secrets in Code
- All API keys, passwords, DB URLs → environment variables only
- Reference via `${ENV_VAR}` in YAML
- `.env` must be in `.gitignore`

### R2 — Stable Dependencies Only
- Always use GA (Generally Available) Spring Boot releases
- Never use SNAPSHOT/RC/Milestone versions unless explicitly required

### R3 — Flyway Manages Schema
- NEVER use `ddl-auto: create` or `update` in production
- All schema changes go through Flyway migration files
- Migration files are IMMUTABLE once committed

### R4 — Service Layer Owns Business Logic
- Controllers only handle HTTP: parse request → call service → return response
- All business logic lives in Service layer
- Repositories only do data access (no business logic)

### R5 — DTOs for Data Transfer
- Never expose JPA entities directly to the view layer
- Use DTOs for form inputs and view models

### R6 — Test with H2
- Unit tests use H2 in-memory DB, not PostgreSQL
- Integration tests use `@SpringBootTest` with H2 profile
- Never run tests against the production database

### R7 — Security First
- Passwords encoded with BCryptPasswordEncoder
- CSRF enabled for all POST/PUT/DELETE forms
- Hidden CSRF input in every Thymeleaf form: `th:action`

### R8 — No Hallucination
- Only implement features listed in the requirements
- If unsure about a requirement → ask, don't guess

### R9 — DRY (Don't Repeat Yourself)
- Reuse Thymeleaf fragments for navbar, footer, layout
- Reuse service methods; don't duplicate logic across controllers

### R10 — Update context.md on Every Error
- Any mistake made → immediately documented in `.agent/context.md`
- Include: problem, root cause, solution, prevention rule
