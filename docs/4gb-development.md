# 4 GB RAM Development Guide

Developing a microservices architecture on a strict 4 GB RAM machine requires discipline and running only the necessary components at any given time. This guide outlines the workflow to successfully develop the Intelligent Product Recommendation System without exceeding memory limits.

## Prerequisites
- Windows 10/11 with 4 GB RAM.
- Java 21 and Maven.
- Node.js 18+ (for frontend).
- Python 3.12 (for ml-service).
- Tesseract (installed and accessible in system PATH for OCR).
- PostgreSQL 16 installed locally.
- Do NOT use Docker, Redis, Kafka, Kubernetes, or Elasticsearch.

## PostgreSQL Setup
- Run PostgreSQL directly as a local system service. Do not run it in Docker.
- Create databases (schemas) for each service as defined in their application properties (e.g., `user_db`, `job_db`, etc.).
- Flyway migrations run automatically on service startup to create and maintain tables.

## Environment Variables
Ensure the following variables are set. Use a `.env` file or export them directly:
- `JWT_SECRET`: The shared secret for signing JWT tokens.
- `X_INTERNAL_KEY`: The secret key for service-to-service internal endpoints.
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`: Point these to your local PostgreSQL instance.

## One-Service-At-A-Time Workflow
To fit within 4 GB RAM, do not run the entire microservices stack simultaneously:
- Start only the specific service you are currently developing or testing.
- Rely on lightweight local stubs for downstream dependencies. If a downstream service is required, keep it mocked or stopped unless doing a brief integration test.
- Stop the current service before launching another.

## How to Test Each Service
- Use `mvn clean test` and `mvn package` to run tests and build the service.
- Prefer unit tests over heavy integration tests.
- When manual API testing is needed, use `mvn spring-boot:run` to start the single service, perform tests via Postman or cURL, and then shut it down.

## How to Run ML Service
- Activate the Python virtual environment (`.venv\Scripts\activate`).
- Run the API: `uvicorn app.main:app --host 127.0.0.1 --port 8000`
- Keep memory usage low. The ML service uses dictionary/rule-based extraction first. Ensure Tesseract is installed and process files one page at a time.
- Shut down the ML service when not directly working on extraction or semantic matching.

## How to Run Frontend
- Execute `npm run dev` in the `frontend/` directory.
- The frontend defaults to mock mode (`NEXT_PUBLIC_USE_MOCKS=true`), allowing you to test UI features (like the notifications bell, unread count, empty states) without needing any backend services running.

## How to Use Gateway
- When you need to test edge routing or a full end-to-end flow, start the `gateway` (port 8080) and exactly ONE downstream service at a time (e.g., `auth-service` on port 8081).
- Set `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080` in the frontend environment, and `NEXT_PUBLIC_USE_MOCKS=false`.

## Common Errors and Troubleshooting
- `OutOfMemoryError`: Ensure no other Java or Node processes are running. Use Task Manager or `jps` to find and kill lingering Java processes.
- Background process bloat: Disable unnecessary background apps. Close IDE tabs if memory becomes constrained.
- `Connection Refused`: Verify that the specific service you are trying to reach is actually running, as they are mostly off by default.
- Tesseract errors: Ensure Tesseract is installed natively on Windows and added to your `PATH`.
- ML Model loading: If `fastembed` is enabled, the model is lazy-loaded on the first request. The first request will take longer and consume more memory.
