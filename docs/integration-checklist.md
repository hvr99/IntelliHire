# Frontend integration checklist

The frontend defaults to mock mode. Set `NEXT_PUBLIC_USE_MOCKS=false` and set `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080` when using the gateway. If the gateway is not used, configure the six public service URLs instead.

## Environment

- [ ] Copy `.env.frontend.example` to the frontend environment configuration.
- [ ] Supply a non-default `JWT_SECRET` consistently to auth and resource services.
- [ ] Set `CORS_ALLOWED_ORIGINS=http://localhost:3000` for each browser-facing Spring service.
- [ ] Confirm PostgreSQL is running and the service schema migration has completed.

## Verify one service at a time

1. **Auth (`8081`)**
   - [ ] Register a `JOB_SEEKER` account.
   - [ ] Register a `RECRUITER` account.
   - [ ] Log in and confirm the token is stored in session storage.
   - [ ] Confirm refresh/navigation restores the session and a 401 returns to `/login`.
2. **Profile (`8082`)**
   - [ ] Open Profile as a job seeker.
   - [ ] Confirm empty profiles load successfully.
   - [ ] Save headline, summary, and location; reload and verify persistence.
3. **Jobs (`8083`)**
   - [ ] Search public jobs as a job seeker.
   - [ ] Create and edit a job as a recruiter.
   - [ ] Confirm recruiter job lists use `/api/jobs/mine`.
4. **Applications (`8084`)**
   - [ ] Apply once as a job seeker.
   - [ ] Confirm applications appear under My applications.
   - [ ] Open recruiter applications for a selected job and change status.
5. **Recommendations (`8085`)**
   - [ ] Open recommendations as a job seeker.
   - [ ] Verify pagination/minimum-score requests and one-job details.
   - [ ] Confirm matched skills, missing skills, and skill gap render.

## Gateway verification

- [ ] Start only gateway and the one downstream service being verified.
- [ ] Confirm `/api/auth/**`, `/api/profile/**`, `/api/candidates/**`, `/api/jobs/**`, `/api/applications/**`, `/api/recommendations/**`, `/api/recruiter/**`, and `/api/notifications/**` route correctly.
- [ ] Confirm `/internal/**` returns 403 through the gateway.
- [ ] Confirm an `OPTIONS` request from `http://localhost:3000` receives the configured CORS headers.
- [ ] Invalid public routes return 404.
- [ ] With gateway CORS enabled, per-service browser CORS can be disabled; keep service-to-service calls direct.

## Direct-service fallback

The typed client keeps service URLs independent. When gateway routes are available, set the individual variables to the gateway origin and keep the existing path contracts, or provide route-specific gateway origins without changing UI code.
