# Paw-Hub v1 Design Specification

**Date:** 2026-04-27
**Status:** Approved

## Overview

Paw-Hub is a test result review application providing automated test result collection, analysis, and summary. It targets QA engineers as primary users, with read-only access for other roles (developers, managers) to view analysis results, trend charts, and summaries.

## Requirements Summary

| # | Requirement | Detail |
|---|-------------|--------|
| R1 | Test result collection | REST API accepting JUnit XML / JSON |
| R2 | Basic statistics | Pass rate, failure rate, duration stats |
| R3 | Trend analysis | Daily/weekly trend snapshots |
| R4 | Regression detection | Run-level (sigma) and case-level detection |
| R5 | Flaky test detection | Transition-based scoring with retry weighting |
| R6 | Failure clustering | Normalized error hash grouping |
| R7 | Multi-tenancy | Org → Team → Project hierarchy, role-based access (ADMIN/QA/VIEWER) |
| R8 | JWT authentication | Bearer token auth for web and API |
| R9 | Retry support | Multiple attempts per test case within a run |
| R10 | Failure triage | QA can annotate failed cases with failure category, issue link, and comments |

## Architecture

Modular monolith: single Spring Boot application + Next.js frontend.

```
CI Systems (GitHub Actions / Jenkins / GitLab CI)
    │  JUnit XML / JSON
    ▼
POST /api/v1/projects/{id}/test-results
    │
    ▼
┌──────────────────────────────────────┐
│         Spring Boot Application       │
│  ┌──────────┐ ┌──────────┐ ┌───────┐ │
│  │Collection│ │Analysis  │ │Report │ │
│  │          │ │Engine    │ │       │ │
│  │ Parse    │ │ Trends   │ │Summary│ │
│  │ Validate │ │ Regression│ │Export │ │
│  │ Store    │ │ Flaky    │ │       │ │
│  │          │ │ Clusters │ │       │ │
│  └──────────┘ └──────────┘ └───────┘ │
│  ┌──────────────────────────────────┐│
│  │        JPA Repository            ││
│  └──────────────────────────────────┘│
│  ┌──────────┐ ┌────────────────────┐ │
│  │ Auth     │ │ Async Executor     │ │
│  └──────────┘ └────────────────────┘ │
│             │                         │
│             ▼                         │
│         H2 (→ PostgreSQL later)       │
└──────────────────────────────────────┘
    │  REST API (JSON)
    ▼
┌──────────────────────────────────────┐
│          Next.js Frontend             │
│  Dashboard │ Explorer │ Trends │ Config│
└──────────────────────────────────────┘
```

- Module boundaries are internal Java packages with clean interfaces. Modules can be extracted into microservices later.
- Analysis runs asynchronously — upload returns immediately, analysis completes in background.

## Data Model

### Multi-Tenancy

| Entity | Key Fields |
|--------|------------|
| Organization | id, name, created_at |
| Team | id, name, org_id (FK) |
| Project | id, name, team_id (FK), api_key |
| User | id, username, email, password_hash |
| Membership | user_id, team_id, role (ADMIN/QA/VIEWER) |

### Test Results

**TestRun**

| Field | Type | Note |
|-------|------|------|
| id | Long (PK) | |
| project_id | Long (FK) | |
| run_identifier | String | build#, commit SHA, or CI job ID |
| branch | String | |
| commit_sha | String | |
| triggered_by | String | |
| environment | String | dev / staging / prod |
| total_cases | int | |
| passed | int | Final-attempt count |
| failed | int | Final-attempt count |
| skipped | int | |
| duration_ms | long | Total wall-clock duration |
| status | enum | PASS / FAIL / ERROR |
| raw_xml | CLOB | Original JUnit XML |
| created_at | Instant | |

**TestExecution**

| Field | Type | Note |
|-------|------|------|
| id | Long (PK) | |
| test_run_id | Long (FK) | |
| attempt | int | 1-based retry counter |
| suite_name | String | |
| class_name | String | |
| test_name | String | |
| case_number | String | External test case ID (e.g., JIRA key "PROJ-1234") |
| status | enum | PASS / FAIL / SKIP / ERROR |
| duration_ms | long | |
| error_message | String | |
| stack_trace | CLOB | |
| error_type | String | Exception class name |

Unique key: `(test_run_id, suite_name, class_name, test_name, attempt)`

### Analysis Results (Computed)

**TrendSnapshot** — Daily/weekly aggregated stats per project per environment.

| Field | Type |
|-------|------|
| id, project_id, date, environment, period_type (DAILY/WEEKLY) | |
| pass_rate, failure_rate, avg_duration, retry_rate | |

**FlakyTestRecord** — Detected flaky tests.

| Field | Type |
|-------|------|
| id, test_case_key, project_id | |
| flaky_score (0.0–1.0), transition_count, retry_pass_count | |
| last_detected_at | |

**FailureCluster** — Grouped failures by normalized error.

| Field | Type |
|-------|------|
| id, project_id, cluster_key (error hash) | |
| representative_error, occurrence_count | |
| first_seen, last_seen | |

**FailureTriage** — QA annotation on failed test executions.

| Field | Type | Note |
|-------|------|------|
| id | Long (PK) | |
| test_execution_id | Long (FK → TestExecution, unique) | One-to-one |
| triage_status | enum | UNTRIAGED, NEW_BUG, KNOWN_ISSUE, SCRIPT_ISSUE, DATA_ISSUE, ENV_ISSUE, CR, OTHER |
| issue_link | String | URL to JIRA or issue tracker |
| comment | String | QA notes |
| annotated_by | Long (FK → User) | |
| created_at, updated_at | Instant | |

## API Design

Base path: `/api/v1`

### Collection

| Method | Path | Description |
|--------|------|-------------|
| POST | `/projects/{projectId}/test-results` | Upload JUnit XML / JSON. Returns `{ testRunId }` |
| GET | `/projects/{projectId}/test-runs/{runId}` | Run detail with executions |
| GET | `/projects/{projectId}/test-runs` | Run list (params: branch, status, environment, from, to, page, size) |

### Analysis

| Method | Path | Description |
|--------|------|-------------|
| GET | `/projects/{projectId}/trends` | Trend data (params: period=daily|weekly, days, environment) |
| GET | `/projects/{projectId}/flaky-tests` | Flaky tests ranked by score |
| GET | `/projects/{projectId}/failure-clusters` | Failure clusters ranked by count |

### Triage

| Method | Path | Description |
|--------|------|-------------|
| PUT | `/projects/{projectId}/test-executions/{executionId}/triage` | Create or update triage for a failed execution |
| GET | `/projects/{projectId}/test-executions/{executionId}/triage` | Get triage detail for an execution |
| GET | `/projects/{projectId}/triage-summary` | Aggregated triage stats (count by category, untriaged count) |

### Reporting

| Method | Path | Description |
|--------|------|-------------|
| GET | `/projects/{projectId}/summary` | Time-range summary with top failures and trends |
| GET | `/projects/{projectId}/export` | Export report (params: format=json|pdf, from, to) |

### Auth & Management

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/login` | JWT login |
| POST | `/auth/register` | User registration |
| CRUD | `/orgs`, `/teams`, `/projects` | Tenant management |

Authentication: `Authorization: Bearer <jwt>` header.

## Analysis Engine

All analysis runs asynchronously after test result ingestion. Each step is idempotent.

### Step 1 — Trend Analysis

Group TestRun by (project, environment, date/period) → compute pass_rate, failure_rate, avg_duration, retry_rate → upsert TrendSnapshot.

### Step 2 — Regression Detection

- **Run-level:** pass_rate < (rolling_avg - 2 * sigma) → flag regression
- **Case-level:** test that passed N consecutive prior runs now failed → flag regression
- Sigma and window configurable per project.

### Step 3 — Flaky Test Detection

Per test_case_key (suite+class+name), over last N runs:
- Count PASS ↔ FAIL transitions
- flaky_score = transitions / total_appearances
- Weight retry-then-pass cases higher
- Score ≥ threshold → record as flaky.

### Step 4 — Failure Clustering

1. Normalize error messages: strip timestamps, hex IDs, line numbers, numeric values
2. Hash normalized message → cluster_key
3. Group failures by cluster_key
4. Pick most frequent variant as representative
5. Update FailureCluster occurrence_count, first_seen, last_seen.

## Frontend Pages

| Page | Content |
|------|---------|
| Dashboard | KPI cards (total runs, pass rate, flaky count, regressions), pass rate trend chart, recent regressions list, triage breakdown widget |
| Test Explorer | Per-run test table with filter (status, env, attempt), search by name, inline triage modal for failed cases, error detail popup |
| Trends & Analysis | Multi-line pass rate by environment, duration trend, top flaky tests table, failure clusters table |
| Settings | Project config, API key display, analysis thresholds, team member list |

## Deployment

Docker Compose with two services:

```yaml
services:
  backend:
    build: ./backend
    ports: ["8080:8080"]
    environment:
      SPRING_PROFILES_ACTIVE: default  # H2
    healthcheck: GET /actuator/health

  frontend:
    build: ./frontend
    ports: ["3000:3000"]
    environment:
      NEXT_PUBLIC_API_URL: http://backend:8080
    depends_on: [backend]
```

- No external DB dependency in v1 — H2 embedded
- To switch to PostgreSQL: add `postgres` service, set `SPRING_PROFILES_ACTIVE=prod`
- Kubernetes-ready: backend is stateless once on PostgreSQL, frontend is stateless

## Technology Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 21, Spring Boot 3.x |
| Database | H2 (dev), PostgreSQL (future production) |
| Frontend | Next.js (React), TypeScript |
| Auth | Spring Security + JWT |
| Build | Maven (backend), npm/pnpm (frontend) |
| Deployment | Docker Compose |

## Out of Scope (v1)

- Email/webhook notifications
- PDF export (JSON export only)
- SSO / OAuth integration
- Test comparison across branches (diff view)
- Custom dashboard widgets
