# API Contracts

This document specifies the REST API contracts for all microservices in the platform. All inter-service communication must strictly adhere to these contracts.

---

## 1. Gateway (`:8080`)
*Reverse proxy routing requests to internal microservices and validating edge JWT tokens.*

### Endpoints (Placeholder)
- `/*` - Forwarding routes mapped per downstream service

---

## 2. Auth Service (`:8081`)
*Handles user registration, authentication, token refresh, and JWT issuance.*

### Endpoints (Placeholder)
- `POST /api/v1/auth/register` - Register a new user (`JOB_SEEKER` or `RECRUITER`)
- `POST /api/v1/auth/login` - Authenticate and retrieve access/refresh tokens
- `POST /api/v1/auth/refresh` - Refresh an expired access token
- `POST /api/v1/auth/validate` - Validate token validity (internal verification)

---

## 3. User Service (`:8082`)
*Manages candidate and recruiter profiles, skills, and preferences.*

### Endpoints
- `POST /api/profile/resume` - Forward a resume upload to ml-service and save extracted skills as `PENDING` suggestions (`JOB_SEEKER`)
- `POST /api/profile/certificates` - Forward a certificate upload to ml-service and save extracted skills as `PENDING` suggestions (`JOB_SEEKER`)
- `GET /api/profile/skill-suggestions` - List the current user's skill suggestions
- `POST /api/profile/skill-suggestions/confirm` - Confirm selected suggestion IDs; all other pending suggestions are rejected

### Endpoints (Placeholder)
- `GET /api/v1/users/me` - Fetch profile for authenticated user
- `PUT /api/v1/users/me` - Update profile data
- `GET /api/v1/users/{userId}` - Fetch public profile (restricted view)
- `GET /api/v1/internal/users/{userId}` - Internal endpoint to fetch user profile details (`X-Internal-Key`)

---

## 4. Job Service (`:8083`)
*Manages job postings, requirements, categories, and recruiter job management.*

### Endpoints (Placeholder)
- `POST /api/v1/jobs` - Create a new job posting (`RECRUITER`)
- `GET /api/v1/jobs` - Search and list active job postings (`JOB_SEEKER`, public)
- `GET /api/v1/jobs/{jobId}` - Get job details by ID
- `PUT /api/v1/jobs/{jobId}` - Update job posting (`RECRUITER`)
- `DELETE /api/v1/jobs/{jobId}` - Close/archive job posting (`RECRUITER`)
- `GET /api/v1/internal/jobs/{jobId}` - Internal endpoint to fetch job requirements for matching (`X-Internal-Key`)

---

## 5. Application Service (`:8084`)
*Tracks job applications submitted by job seekers and candidate pipeline for recruiters.*

### Endpoints
- `POST /api/applications` - Submit `{ "jobId": "..." }` (`JOB_SEEKER`)
- `GET /api/applications/me` - List applications submitted by current job seeker
- `POST /api/applications/{applicationId}/withdraw` - Withdraw own application (`JOB_SEEKER`)
- `GET /api/applications/job/{jobId}` - List applications for a recruiter's own posting (`RECRUITER`)
- `PATCH /api/applications/{applicationId}/status` - Update status with `{ "status": "SHORTLISTED" }` (`RECRUITER`)
- `GET /internal/jobs/{jobId}/applications` - Internal application lookup; requires `X-Internal-Key`

## 6. Recommendation Service (`:8085`)
*Provides deterministic weighted job recommendations; stub downstream mode is available before ML integration.*

### Endpoints
- `GET /api/recommendations?page=0&size=20&minScore=50` - Return paginated job recommendations for the authenticated `JOB_SEEKER`
- `GET /api/recommendations/jobs/{jobId}` - Return one recommendation and skill gap for the authenticated `JOB_SEEKER`

Responses include `jobId`, `title`, `company`, `location`, `matchPercent`, `matchedSkills`, `missingSkills`, `skillGap`, `semanticScore`, and `semanticUsed`. `semanticScore` is the ML cosine score from 0 to 1 and may be `null` when unavailable.

The service uses `RECOMMENDATION_CLIENT_MODE=stub` by default. Set it to `rest` to use user-service and job-service clients.

Semantic matching is optional and defaults to `RECOMMENDATION_SEMANTIC_MODE=noop`. Set it to `rest` to call ml-service `/similarity`; `ML_SERVICE_URL`, `RECOMMENDATION_SEMANTIC_TIMEOUT_MS`, and `RECOMMENDATION_SEMANTIC_BLEND` configure the client and blend. The default blend is `0.20`: `matchPercent = weightedScore * 0.80 + semanticScore * 100 * 0.20` when semantic data is available. Semantic failures return the exact weighted score and do not affect basic recommendations. One batched ML request is made per recommendation request.

### Recruiter candidate ranking

- `GET /api/recruiter/jobs/{jobId}/candidates?page=0&size=20` - Rank applicants for a recruiter-owned job (`RECRUITER`)

The response contains `candidateId`, `matchPercent`, `semanticScore`, `semanticUsed`, `matchedSkills`, `missingSkills`, and `applicationStatus`, plus `skippedCandidates` for profiles that could not be retrieved. Ranking uses the same `MatchingEngine` and semantic blend configuration as seeker recommendations. Missing candidate profiles are skipped without failing the request; ML failure falls back to weighted scoring.

Recommendation REST clients use `GET /internal/profiles/{userId}`, `GET /internal/profiles/{userId}/skills`, and `GET /internal/jobs/open`; internal requests require `X-Internal-Key`.

---

## 7. Notification Service (`:8086`)
*Dispatches lightweight in-app notifications and alerts for application status updates.*

### Endpoints
- `GET /api/notifications` - Retrieve notifications for the authenticated user
- `POST /api/notifications/{id}/read` - Mark the user's notification as read
- `POST /api/notifications/read-all` - Mark all notifications for the authenticated user as read
- `POST /internal/notifications` - Create a notification; requires `X-Internal-Key`

Only `RECOMMENDED_JOB` and `APPLICATION_STATUS_CHANGED` notification types are accepted. Recommendation notifications are disabled by default via `RECOMMENDATION_NOTIFICATIONS_ENABLED=false`; when created, the service prevents duplicate notifications for the same user and job.

---

## 8. ML Service (`:8000`)
*FastAPI microservice providing CPU-friendly keyword extraction, candidate-job matching, and resume parsing.*

### Endpoints
- `GET /health` - Health check
- `POST /normalize` - Normalize a JSON array of skill strings; returns de-duplicated `{skill, matched}` entries while preserving unknown skills
- `POST /extract/resume` - Extract text and dictionary-matched skills from a PDF or DOCX multipart upload named `file`
- `POST /extract/certificate` - Extract certificate text, title, issuer, and dictionary-matched skills from a PDF, PNG, JPG, or JPEG multipart upload named `file`
- `POST /embed` - Generate embeddings for a JSON array of text strings
- `POST /similarity` - Return cosine similarity for one candidate text against a batch of jobs
- `POST /api/v1/ml/parse-resume` - Reserved for a future compatibility route; not implemented in this phase
- `POST /api/v1/ml/match-score` - Compute match score between candidate profile and job requirements

`/extract/resume` returns `textLength`, detected `sections`, and `matchedSkills` entries containing canonical `skill` names and a confidence value. Files are limited by `MAX_RESUME_SIZE_BYTES` (5 MiB by default).

`/extract/certificate` returns `rawText`, `certificateTitle`, `issuer`, `detectedSkills`, `confidence`, and `needsReview`. PDF text layers are preferred; image-only PDFs and image files use one-page CPU OCR with system Tesseract. OCR-dependent requests return `503` when Tesseract is unavailable.

`/health` returns `status` and `modelLoaded`. The embedding provider defaults to a deterministic fake provider, so no model is loaded during startup. Set `EMBEDDING_PROVIDER=fastembed` to enable real embeddings; the configured model is loaded lazily on the first `/embed` request. Configure `EMBEDDING_MODEL_NAME`, `EMBEDDING_CACHE_DIR`, `EMBEDDING_MAX_BATCH_SIZE`, and `EMBEDDING_MAX_TEXT_LENGTH` through the environment. `/embed` returns `embeddings`, `count`, and `dimension`, and rejects empty, oversized, or over-limit requests.

`/similarity` accepts `{ "candidateText": "...", "jobs": [{ "id": "...", "text": "..." }] }` and returns `{ "matches": [{ "jobId": "...", "similarity": 0.0 }] }`. The candidate plus jobs are embedded in one bounded batch; request state is not stored. `SIMILARITY_MAX_JOBS` controls the job batch limit.

---

## 9. Frontend (`:3000`)
*Next.js web application for Job Seekers and Recruiters.*
