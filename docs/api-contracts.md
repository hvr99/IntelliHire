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

### Endpoints (Placeholder)
- `POST /api/v1/applications` - Submit a new job application (`JOB_SEEKER`)
- `GET /api/v1/applications/me` - List applications submitted by current job seeker
- `GET /api/v1/applications/job/{jobId}` - List applications for a specific posting (`RECRUITER`)
- `PATCH /api/v1/applications/{applicationId}/status` - Update application status (`RECRUITER`)

---

## 6. Recommendation Service (`:8085`)
*Calculates recommendation scores, coordinates with ML service, and provides ranked job lists.*

### Endpoints (Placeholder)
- `GET /api/v1/recommendations/jobs` - Get personalized job recommendations for authenticated seeker
- `GET /api/v1/recommendations/candidates/{jobId}` - Get ranked candidates for a specific posting (`RECRUITER`)

---

## 7. Notification Service (`:8086`)
*Dispatches in-app notifications and alerts for application status updates.*

### Endpoints (Placeholder)
- `GET /api/v1/notifications` - Retrieve notifications for authenticated user
- `PATCH /api/v1/notifications/{notificationId}/read` - Mark notification as read
- `POST /api/v1/internal/notifications` - Internal endpoint to dispatch a notification (`X-Internal-Key`)

---

## 8. ML Service (`:8000`)
*FastAPI microservice providing CPU-friendly keyword extraction, candidate-job matching, and resume parsing.*

### Endpoints (Placeholder)
- `POST /api/v1/ml/parse-resume` - Extract text, skills, and experience from resume file
- `POST /api/v1/ml/match-score` - Compute match score between candidate profile and job requirements
- `GET /health` - Health and readiness check

---

## 9. Frontend (`:3000`)
*Next.js web application for Job Seekers and Recruiters.*
