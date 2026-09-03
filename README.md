# NexusHR

NexusHR is a full-stack HR management platform covering employee and department administration, attendance tracking with real-time updates, leave management, payroll processing (including a PF/ESI statutory challan export), and basic performance management (goals + reviews). It's built as a learning/demo project that leans on production-grade patterns — JWT authentication with Argon2id password hashing, Redis-backed refresh-token rotation, Spring Batch for payroll runs, optimistic locking on shared balances, AOP-based audit trails on sensitive mutations, and a real Testcontainers-backed integration test suite — rather than toy shortcuts.

**Live**: backend [`https://nexushr-vj7w.onrender.com`](https://nexushr-vj7w.onrender.com), frontend [`https://nexus-hr-gilt.vercel.app`](https://nexus-hr-gilt.vercel.app). Both auto-deploy on every push to `main`.

## Architecture

- **Backend**: Spring Boot 3 (`Employee/` module), Java 21.
- **Frontend**: React 19 + TypeScript + Vite (`frontend/` module), TanStack Query, Tailwind v4, React Router.
- **Database**: PostgreSQL, schema managed via Flyway migrations (`Employee/src/main/resources/db/migration`).
- **Cache / pub-sub**: Redis — session/refresh-token storage and real-time attendance events.
- **Batch processing**: Spring Batch drives monthly payroll generation (listener, scheduler, and audit trail).
- **Security**: Spring Security 6 + JWT (Argon2id password hashing, refresh-token rotation stored in Redis with a `jti` claim guaranteeing token uniqueness, per-IP rate limiting on auth endpoints — proxy-aware via `ForwardedHeaderFilter`, since the deployed backend sits behind Render/Cloudflare).
- **Audit**: `@Auditable` AOP annotation + aspect records every Employee/Department/Payroll/Goal/Review mutation to an `audit_log` table (who, what, when).
- **Testing**: JUnit5 + Mockito unit tests, plus real integration tests against Testcontainers-provisioned Postgres + Redis (shared across test classes via a static-initializer singleton, not `@Container`'s per-class lifecycle — see `AbstractIntegrationTest`'s comment for why that distinction matters).
- **Real-time**: Server-Sent Events (SSE) for live attendance updates, via `@microsoft/fetch-event-source` on the frontend (native `EventSource` can't send the `Authorization` header this endpoint needs).
- **Deployment**: Render (backend + `ai-service`, both Docker) + Vercel (frontend), all auto-deploying from `main`.

## Module Status

| Module | Status | Notes |
|---|---|---|
| Auth | Done | JWT + Argon2id, refresh rotation in Redis, `jti`-guaranteed token uniqueness, per-IP rate limiting on login/refresh (proxy-aware) |
| Employee/Dept CRUD | Done | Cascading deletes fixed (employee-referencing FKs now `ON DELETE CASCADE`); audited via `@Auditable` |
| Attendance + SSE | Done | Live feed verified via real Playwright browser testing, including a genuine multi-client broadcast test |
| Leave | Done | Optimistic locking on balances verified under genuine two-thread concurrency (`LeaveApprovalIntegrationTest`), draft→submit→approve/reject flow, audited |
| Payroll | Done | Batch generation, approve/lock/pay lifecycle, self-service payslip download, PF/ESI challan Excel export, audited; batch job verified via a real `JobLauncherTestUtils` run against Testcontainers |
| Performance Management | Done | Goals (self-service CRUD) + Performance Reviews (manager/admin-authored, 1–5 rating), modeled on the Department module's structure; frontend verified live (goal creation + review submission round-trip on the deployed backend) |
| Frontend | Done | Auth, Employees/Departments, Attendance, Leave, Payroll, Performance, AI Insights, Admin dashboard — all with a working backend; dark mode (cookie-backed, no flash-of-wrong-theme) and an accessibility pass (WCAG AA contrast checked/fixed, form labels, focus-visible rings, `aria-live` regions) across every page; deployed to Vercel with SPA routing fixed |
| Admin dashboard | Done | Department cost breakdown (recharts, with an explicit "Unassigned" bucket rather than hiding the ~80% of employees with no department on file) + headcount/leave/payroll/attrition summary cards; folded into the existing `/` dashboard route for privileged roles |
| Deployment | Done | Backend + `ai-service` on Render (two separate Docker services), frontend on Vercel, all live and auto-deploying on push; local data migrated to Render's Postgres |
| Test suite | Done | 3 integration test classes (payroll batch, leave approval + concurrency, auth flow) + 5 unit test classes (29 tests total, all passing) |
| Postman collection | Done | `postman/NexusHR.postman_collection.json` — auth, employee, attendance, leave, payroll flows |
| AI attrition scoring | Done | FastAPI/scikit-learn sidecar (`ai-service/`) scores attrition risk; deployed as its own Render Web Service (model trained fresh at Docker build time — the `.joblib` is gitignored, not committed), wired into a Spring Batch job that persists scores via `GET /api/ai/attrition-scores`. Verified live end-to-end: 42 real scores, correctly ranked, rendering on the deployed AI Insights dashboard. |
| RAG chatbot | Done | Spring AI + pgvector + Gemini free tier (Developer API, not Vertex) over seeded HR policy docs; `POST /api/hr-chat`, any authenticated employee. Degrades gracefully (clear message, not a 500 or a crash) if ingestion never succeeded or a live Gemini call fails. Verified live with grounded real answers matching the seeded policy docs. |
| Kubernetes/EKS | Not started | Render/Vercel is the live deployment target; EKS was an earlier plan not yet pursued |

## Local Run

### Backend

1. Start Postgres and Redis:
   ```
   cd Employee
   docker-compose up -d postgres redis
   ```
2. Set required environment variables (see `Employee/src/main/resources/application.properties` for the full list):
   - `JWT_SECRET_KEY` — **required**, no default; the app fails fast at startup without it. Generate one with `openssl rand -base64 32`.
   - `GEMINI_API_KEY` — **required**, no default; the RAG chatbot (`/api/hr-chat`) needs it to reach Gemini's free-tier Developer API. Get one at [aistudio.google.com/apikey](https://aistudio.google.com/apikey).
   - `MAIL_USERNAME` / `MAIL_PASSWORD` — optional, blank by default (leave/payroll email notifications no-op without them).
   - Datasource and Redis default to `localhost` with the credentials in `application.properties`, matching the ports `docker-compose.yml` exposes — no overrides needed for a local, non-containerized run.
3. Run the app:
   ```
   cd Employee
   mvn spring-boot:run
   ```

To run the whole stack (app included) in Docker, `docker-compose up` from `Employee/` — the compose file passes `JWT_SECRET_KEY` through from your shell/`.env`, so it still needs to be set in your environment first.

### Frontend

```
cd frontend
npm install
npm run dev
```

Requires `VITE_API_BASE_URL` pointing at a running backend (`frontend/.env.example`).

### Tests

```
cd Employee
mvn test
```

Integration tests need Docker running (Testcontainers provisions Postgres + Redis automatically — no manual setup needed, and no port conflicts with the `docker-compose` services above since Testcontainers uses random host ports).

## Known Limitations

- `RateLimitFilter`'s token buckets are in-memory per app instance — not shared across multiple instances behind a load balancer, and reset on restart.
- `LeaveServiceImpl.carryForwardBalances()` and `closeLeaveCycle()` exist but are still unwired dead code — not called from anywhere. This is separate from `LeaveScheduler`, which *is* actually wired via `@Scheduled` cron jobs (monthly earned-leave accrual, year-end carry-forward with a 10-day cap) and covers similar ground with different logic.
- PF/ESI challan export computes employer-side PF/ESI contributions from the employee's *current* salary structure at export time (not persisted anywhere on the payroll record itself). If a salary structure is revised after a payroll record was generated, the challan's employer columns reflect the current structure while the employee PF/ESI columns still reflect what was locked in at generation time.
- Render's free tier spins down on inactivity — the first request after idle time can take 10–90+ seconds (cold start) before the backend responds. `ai-service` is a separate free-tier Render service with the same behavior, so the nightly scheduled attrition-scoring job's first call each day may also hit a cold-start delay — not a bug, just a free-tier tradeoff.
- `GET /api/employee/all` is open to any `MANAGER`, not just managers of the employee being reviewed — there's no manager-to-report or manager-to-department relationship modeled anywhere in the schema (`Employee` has no "reports to" field; `Department` has no "managed by" field), so a MANAGER using the Performance review UI can browse the full employee list rather than a scoped subset. Narrowing this would mean modeling that relationship first, not just adding a filter.
