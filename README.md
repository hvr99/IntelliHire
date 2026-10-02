# Intelligent Job Recommendation System (Microservices)

## Project Purpose
A modular microservices-based job recruitment and intelligent recommendation platform. The platform connects Job Seekers with Recruiters through targeted job postings, structured applications, automated notifications, and CPU-efficient machine learning matching.

## Repository Structure
```
├── auth-service/           # Spring Boot: Authentication & JWT issuance (Port 8081)
├── user-service/           # Spring Boot: User profiles & role management (Port 8082)
├── job-service/            # Spring Boot: Job postings & catalog (Port 8083)
├── application-service/    # Spring Boot: Job application tracking (Port 8084)
├── recommendation-service/ # Spring Boot: Recommendation aggregation & scoring (Port 8085)
├── notification-service/   # Spring Boot: Event & status notifications (Port 8086)
├── ml-service/             # FastAPI/Python: Lightweight CV parsing & matching (Port 8000)
├── gateway/                # API Gateway / Reverse proxy (Port 8080)
├── frontend/               # Next.js App Router & Tailwind CSS (Port 3000)
├── db/                     # Flyway migrations & shared database scripts
├── shared/                 # Common DTOs, utilities, and contracts
└── docs/                   # Documentation & API contracts
```

## Service Ports
| Service | Technology | Port |
| :--- | :--- | :--- |
| **gateway** | Gateway | 8080 |
| **auth-service** | Spring Boot 3.x | 8081 |
| **user-service** | Spring Boot 3.x | 8082 |
| **job-service** | Spring Boot 3.x | 8083 |
| **application-service** | Spring Boot 3.x | 8084 |
| **recommendation-service** | Spring Boot 3.x | 8085 |
| **notification-service** | Spring Boot 3.x | 8086 |
| **ml-service** | FastAPI / Python 3.12 | 8000 |
| **frontend** | Next.js / TypeScript | 3000 |

## 4 GB RAM Development Workflow
This project is strictly designed to run within a **4 GB RAM development environment**:
- **One-Service-at-a-Time Rule**: Do NOT run all microservices simultaneously. Start only the single service being developed or tested alongside local PostgreSQL.
- **No Heavy Containers or Message Brokers**: Do not use Docker Desktop, Testcontainers, Redis, Kafka, RabbitMQ, or Kubernetes.
- **Service Isolation & Stubs**: When developing or testing a service, use lightweight mock/stub clients for downstream dependencies rather than spinning up other services.
- **Resource Discipline**: Avoid running unnecessary background tools or heavy IDE extensions.
- For detailed instructions, refer to [docs/4gb-development.md](docs/4gb-development.md).

## Documentation
- [API Contracts](docs/api-contracts.md): Complete list of REST endpoints and payloads.
- [Integration Checklist](docs/integration-checklist.md): Step-by-step guide to testing service integrations.
- [4 GB RAM Development Guide](docs/4gb-development.md): Instructions for managing memory effectively.

## Local PostgreSQL Requirement
- PostgreSQL 16 must run locally as a native Windows service or lightweight background process.
- Each microservice owns its own isolated schema within PostgreSQL (no direct cross-schema queries).
- Table migrations are managed independently via Flyway.

## Branch Naming Convention
All work branches must follow the standard naming convention:
```
feature/<name>
```
Example: `feature/auth-jwt-implementation`
