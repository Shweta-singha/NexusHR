# NexusHR Frontend

React frontend for NexusHR, talking to the Spring Boot backend in `../Employee`.

## Stack

Vite + React 19 + TypeScript + TanStack Query + Tailwind v4 + React Router.

## Local development

```bash
npm install
npm run dev
```

Requires `VITE_API_BASE_URL` pointing at a running backend — see `.env.example`.

## Modules

- **Auth** — JWT login with refresh-token rotation, role-based route guards
- **Employees / Departments** — paginated employee list, department hierarchy
- **Attendance** — check-in/out, live SSE feed
- **Leave** — draft → submit → cancel lifecycle, admin approval queue
- **Payroll** — self-service payslip download, admin month view with approve/lock/pay actions

## Deployment

Deployed to Vercel with root directory `frontend`, auto-deploying on every push to `main`. The backend deploys separately to Render from `../Employee`.
