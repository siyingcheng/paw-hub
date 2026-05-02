# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

### Backend (Java 21 + Spring Boot 3.4 + Maven)

```bash
# Run backend (dev profile with H2)
cd backend && ./mvnw spring-boot:run

# Run all tests
cd backend && ./mvnw test

# Run a single test class
cd backend && ./mvnw test -Dtest=TrendAnalysisServiceTest

# Package as JAR
cd backend && ./mvnw package -DskipTests
```

### Frontend (Next.js 15 + React 19 + Tailwind CSS 4)

```bash
# Dev server (defaults to port 3000)
cd frontend && npm run dev

# Build for production
cd frontend && npm run build

# Lint
cd frontend && npm run lint
```

### Docker Compose (full stack)

```bash
docker compose up --build
```

## Architecture Overview

Paw-Hub is a test-results collection and analysis platform. CI/CD pipelines upload JUnit XML reports; the platform tracks pass rates, detects flaky tests, clusters failures, and surfaces regressions.

### Backend (`backend/`)

**Module structure** — the backend is a modular monolith organized by feature area under `com.pawhub.module`:

| Module | Purpose |
|---|---|
| `auth` | User registration/login, JWT, org/team/project/membership entities, RBAC (ADMIN/QA/VIEWER) |
| `collection` | Ingests JUnit XML, creates `TestRun` + `TestExecution` entities, publishes `TestResultCollectedEvent` |
| `analysis` | Async listeners compute trends, detect flaky tests, cluster failures, detect regressions |
| `reporting` | Summary aggregation and export (JSON/CSV) |
| `triage` | Manual triage of individual test failures with status/issue-link/comment |

Each module follows the same layering: `controller` → `service` → `repository`, with `entity` (JPA), `dto` (request/response shapes), and optionally `event` (Spring events).

**Key patterns:**

- **Event-driven analysis**: `CollectionService` publishes `TestResultCollectedEvent` after ingestion. `AnalysisListener` picks it up asynchronously (`@Async("analysisExecutor")`) and fans out to four analysis services — trends, regression, flaky detection, failure clustering. This decouples ingestion latency from analysis work.
- **Unified API response**: All controllers return `ApiResponse<T>` — a record with `success`, `message`, `data`. The frontend `api.ts` helper unwraps `data` automatically and throws on `success: false`.
- **JWT auth**: Stateless. `JwtAuthenticationFilter` extracts the Bearer token, validates via `JwtTokenProvider`, sets `SecurityContextHolder` with userId as principal. Public endpoints: `/api/v1/auth/**`, `/api/v1/projects/*/test-results` (for CI uploads).
- **Database**: H2 file-based in dev (`./data/pawhub`), PostgreSQL in prod. `DataInitializer` (runs only on `default` profile, skips if users exist) seeds an org, team, project, 3 users (alice/bob/carol with pass123), 30 days of test runs with executions, trend snapshots, flaky records, and failure clusters.
- **Configuration**: `application.yml` (dev/H2 defaults) vs `application-prod.yml` (PostgreSQL, env vars for secrets).

### Frontend (`frontend/`)

- **Next.js App Router** with `output: 'standalone'`. All routes live under `src/app/`.
- **Route structure**: `/login`, `/register`, `/projects/[projectId]` (dashboard), `/projects/[projectId]/runs/[runId]`, `/projects/[projectId]/trends`, `/projects/[projectId]/settings` (admin only via RBAC).
- **`src/lib/api.ts`**: Centralized API client. Groups calls by domain (`api.auth.*`, `api.collection.*`, `api.analysis.*`, `api.triage.*`, `api.reporting.*`). Reads `NEXT_PUBLIC_API_URL` env var, attaches JWT Bearer token, unwraps `ApiResponse.data`.
- **`src/lib/auth.ts`**: Client-side token management via `localStorage` with key `pawhub_token`.
- **`src/lib/types.ts`**: All TypeScript interfaces matching backend DTOs.
- **`src/components/layout/Sidebar.tsx`**: Navigation with role-based visibility (Settings link only shown for ADMIN role).
- **Styling**: Tailwind CSS 4, dark theme (gray-950 background).
- **Charts**: Recharts for pass-rate trends.
- **Toast system**: React Context-based toast notifications in `src/lib/toast.tsx`.

### Tenancy Model

`Organization` → `Team` → `Project`. Users belong to teams via `Membership` (with role). Test runs belong to a project. Project-scoped URLs are the primary navigation paradigm.

### CI Ingestion Flow

1. CI pipeline POSTs JUnit XML to `/api/v1/projects/{id}/test-results` (unauthenticated)
2. `JUnitXmlParser` parses XML into `ParseResult` (suite name, class name, test name, status, duration, error details)
3. `CollectionService.ingest()` creates `TestRun` + `TestExecution` rows, publishes event
4. `AnalysisListener` asynchronously runs all 4 analysis pipelines for the project
