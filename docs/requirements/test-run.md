# Test Run

**Priority:** P0 | **Version:** v1

## Overview

A Test Run represents one CI job execution containing multiple test cases. Uploaded via REST API as JUnit XML (or JSON). Each run belongs to a project and targets one deployment environment.

## Data Model

```
TestRun
  id: Long (PK)
  project_id: Long (FK → Project)
  runIdentifier: String (e.g. build number, commit SHA)
  branch: String
  commitSha: String
  triggeredBy: String
  environment: String (dev | staging | prod)
  totalCases: int
  passed: int (final-attempt count)
  failed: int (final-attempt count)
  skipped: int
  durationMs: long
  status: enum (PASS | FAIL | ERROR)
  rawXml: CLOB
  createdAt: Instant
```

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/projects/{id}/test-results` | No | Upload JUnit XML / JSON |
| GET | `/api/v1/projects/{id}/test-runs` | Yes | Paginated run list |
| GET | `/api/v1/projects/{id}/test-runs/{runId}` | Yes | Run detail + executions |

## Acceptance Criteria

### Upload (JUnit XML)

- [ ] Upload valid JUnit XML via `multipart/form-data` → 200, TestRun + TestExecutions stored
- [ ] Upload valid XML via `application/json` body → 200
- [ ] Required param `environment` missing → 400
- [ ] Optional params: `runIdentifier`, `branch`, `commitSha`, `triggeredBy`
- [ ] Project not found → 404
- [ ] Malformed XML → error message in response
- [ ] Empty test suite → 200, run with 0 cases created
- [ ] Run status derived: any FAIL/ERROR → FAIL, else PASS

### JUnit XML Parsing

- [ ] `<testsuite>` attributes parsed: name, tests, failures, errors, skipped, time
- [ ] `<testcase>` attributes parsed: name, classname, time
- [ ] `<failure>` tag → TestStatus.FAIL, message, type, stack trace captured
- [ ] `<error>` tag → TestStatus.ERROR, counted as failure
- [ ] `<skipped>` tag → TestStatus.SKIP
- [ ] No failure/error/skipped tags → TestStatus.PASS
- [ ] Duration converted: XML seconds × 1000 → stored milliseconds
- [ ] Multiple `<testsuite>` elements handled (nested suites)

### Run List

- [ ] `GET /test-runs` returns runs ordered by createdAt DESC
- [ ] Pagination: `?page=0&size=20` (default)
- [ ] Response is `TestRunResponse[]` (summary only, no executions)

### Run Detail

- [ ] `GET /test-runs/{runId}` returns full run + all TestExecutions
- [ ] Executions ordered by attempt ASC
- [ ] Run from different project → 404
- [ ] Non-existent run → 404

### Async Processing

- [ ] Upload returns immediately (doesn't wait for analysis)
- [ ] `TestResultCollectedEvent` published after persistence
- [ ] Analysis pipeline triggered asynchronously

## Out of Scope (v1)

- Retry upload (re-submitting retried cases within same run)
- Run deletion or archival
- Run comparison (diff between two runs)
