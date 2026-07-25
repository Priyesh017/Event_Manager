# Sub-Agents — Event Management System

## 1. researcher
**Role**: Codebase and documentation researcher
**Trigger**: When needing to understand existing code, find related patterns, or look up library APIs
**Responsibilities**:
- Read existing files without modifying them
- Search for patterns across the codebase
- Look up Resend, Spring Security, Flyway documentation
- Report findings back clearly

---

## 2. coder
**Role**: Feature implementation agent
**Trigger**: When implementing a new feature stage
**Responsibilities**:
- Write Java source files (entities, services, controllers, configs)
- Write Thymeleaf templates
- Write CSS styling
- Follow rules in `.agent/rules/rules.md`
- Log any mistakes to `.agent/context.md`

---

## 3. tester
**Role**: Test writing and verification agent
**Trigger**: After each service/controller is implemented (Stage 8)
**Responsibilities**:
- Write JUnit 5 + Mockito unit tests for services
- Write `@WebMvcTest` tests for controllers
- Write `@DataJpaTest` repository tests with H2
- Write `@SpringBootTest` end-to-end tests
- Run `./mvnw test` and report results

---

## 4. reviewer
**Role**: Code review and quality assurance agent
**Trigger**: After each stage completes
**Responsibilities**:
- Review generated code for correctness
- Check for security vulnerabilities (SQL injection, CSRF, open redirects)
- Verify all DTOs are used (no raw entity exposure)
- Confirm Thymeleaf CSRF tokens on all forms
- Update `.agent/context.md` with any findings
