# Test Execution

**Priority:** P0 | **Version:** v1

## Overview

A Test Execution is a single test case execution within a Test Run. Supports retries: a test case can have multiple attempts within the same run. Each execution records status, duration, and error details.

## Data Model

```
TestExecution
  id: Long (PK)
  test_run_id: Long (FK → TestRun)
  attempt: int (1-based retry counter, default 1)
  suiteName: String
  className: String
  testName: String
  caseNumber: String (external test case ID, e.g. JIRA key "PROJ-1234")
  status: enum (PASS | FAIL | SKIP | ERROR)
  durationMs: long
  errorMessage: String (max 4000 chars)
  errorType: String (exception class name)
  stackTrace: CLOB

  unique: (test_run_id, suiteName, className, testName, attempt)
```

## API Endpoints

Executions are returned as nested data within Test Run endpoints:

| Parent Endpoint | Contains |
|-----------------|----------|
| `GET /test-runs/{runId}` | `executions: TestExecutionResponse[]` |

## Acceptance Criteria

### Data Integrity

- [ ] Unique constraint prevents duplicate (run + suite + class + name + attempt)
- [ ] Status values strictly: PASS, FAIL, SKIP, ERROR
- [ ] Attempt number starts at 1
- [ ] caseNumber is optional (null when not provided)
- [ ] Error fields (message, type, stackTrace) nullable for PASS/SKIP

### Retry Handling

- [ ] Multiple executions with same (suite, class, name) but different `attempt` values
- [ ] Analysis uses final-attempt status for pass/fail counts
- [ ] All attempt durations summed for total cost
- [ ] Retry-pass pattern detected in flaky analysis (attempt > 1 and status = PASS)

### Frontend Display (Test Explorer)

- [ ] Each execution shown as a table row
- [ ] Columns: #, Case ID, Suite/Class, Test Name, Attempt, Status, Duration, Error, Triage
- [ ] Status color-coded: PASS=green, FAIL/ERROR=red, SKIP=yellow
- [ ] Failed executions have expandable error detail (message + stack trace)
- [ ] Failed executions have "Triage" button → opens TriageModal
- [ ] Search input filters by testName or className (case-insensitive)
- [ ] Status dropdown filters by PASS/FAIL/SKIP/ERROR

## Out of Scope (v1)

- Bulk retry re-upload (re-running all failed cases in a run)
- Execution-level environment (environment is on TestRun)
