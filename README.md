# NexusHR

NexusHR is a Spring Boot 3 / Java 21 HR management platform covering employee and department administration, attendance tracking with real-time updates, leave management, and payroll processing. It's built as a learning/demo project that leans on production-grade patterns — JWT authentication with Argon2id password hashing, Redis-backed refresh-token rotation, Spring Batch for payroll runs, optimistic locking on shared balances, and audit trails on sensitive state transitions — rather than toy shortcuts.

## Architecture

- **Backend**: Spring Boot 3 (Employee module), Java 21.
- **Database**: PostgreSQL 17, schema managed via Flyway migrations (`Employee/src/main/resources/db/migration`).
- **Cache / pub-sub**: Redis — session/refresh-token storage and real-time attendance events.
- **Batch processing**: Spring Batch drives monthly payroll generation (listener, scheduler, and audit trail).
- **Security**: Spring Security 6 + JWT (Argon2id password hashing, refresh-token rotation stored in Redis, per-IP rate limiting on auth endpoints).
- **Real-time**: Server-Sent Events (SSE) for live attendance updates.

## Module Status

| Module | Status | Notes |
|---|---|---|
| Auth | Done | JWT + Argon2id, refresh rotation in Redis, per-IP rate limiting on login/refresh (Day 3) |
| Employee/Dept CRUD | Done | |
| Attendance + SSE | Done | |
| Leave | Done, hardening in progress | Day 3: optimistic locking on balances, cancelLeave, leave-type-aware carry-forward (unscheduled) |
| Payroll batch | Hardening | Batch listener/scheduler/audit added Day 2; consolidated audit-write into the shared generation helper |
| Performance Management | Not started | |
| AI attrition + RAG | Not started | |
| Frontend | Not started | |
| Kubernetes/EKS | Not started | |

## Local Run

1. Start Postgres and Redis:
   ```
   cd Employee
   docker-compose up -d postgres redis
   ```
2. Set required environment variables (see `Employee/src/main/resources/application.properties` for the full list):
   - `JWT_SECRET_KEY` — **required**, no default; the app fails fast at startup without it. Generate one with `openssl rand -base64 32`.
   - `MAIL_USERNAME` / `MAIL_PASSWORD` — optional, blank by default (leave/payroll email notifications no-op without them).
   - Datasource and Redis default to `localhost` with the credentials in `application.properties`, matching the ports `docker-compose.yml` exposes — no overrides needed for a local, non-containerized run.
3. Run the app:
   ```
   cd Employee
   mvn spring-boot:run
   ```

To run the whole stack (app included) in Docker, `docker-compose up` from `Employee/` — the compose file passes `JWT_SECRET_KEY` through from your shell/`.env`, so it still needs to be set in your environment first.

## Known Limitations

This section is a stub, to be filled in as hardening continues over the remaining days. Known gaps so far:

- `RateLimitFilter`'s token buckets are in-memory per app instance — not shared across multiple instances behind a load balancer, and reset on restart.
- `LeaveServiceImpl.carryForwardBalances()` and `closeLeaveCycle()` exist but aren't wired to any scheduler yet.
- No frontend exists yet; all modules are API-only.
