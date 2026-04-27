# Failure Triage

**Priority:** P1 | **Version:** v1

## Overview

QA engineers annotate failed test executions with a failure category, link to an issue tracker (e.g., JIRA), and free-text comments. Each execution has at most one triage record (upsert pattern).

## Data Model

```
FailureTriage
  id: Long (PK)
  test_execution_id: Long (FK → TestExecution, unique)
  triageStatus: enum (UNTRIAGED, NEW_BUG, KNOWN_ISSUE, SCRIPT_ISSUE,
                       DATA_ISSUE, ENV_ISSUE, CR, OTHER)
  issueLink: String (URL to JIRA or issue tracker)
  comment: String (max 2000 chars)
  annotatedBy: Long (FK → User)
  createdAt: Instant
  updatedAt: Instant
```

## Triage Categories

| Category | Meaning |
|----------|---------|
| UNTRIAGED | Not yet reviewed (default) |
| NEW_BUG | New defect discovered |
| KNOWN_ISSUE | Already tracked bug |
| SCRIPT_ISSUE | Problem with test automation code |
| DATA_ISSUE | Test data problem |
| ENV_ISSUE | Environment issue (dev/staging/prod) |
| CR | Code review finding |
| OTHER | Uncategorized |

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| PUT | `/api/v1/projects/{id}/test-executions/{eid}/triage` | Yes | Create or update triage |
| GET | `/api/v1/projects/{id}/test-executions/{eid}/triage` | Yes | Get triage for execution |
| GET | `/api/v1/projects/{id}/triage-summary` | Yes | Aggregated stats |

## Acceptance Criteria

### Save Triage

- [ ] First triage on execution → 200, triage record created (createdAt set)
- [ ] Update existing triage → 200, fields updated, updatedAt refreshed
- [ ] triageStatus is required (enum validation)
- [ ] issueLink is optional (nullable)
- [ ] comment is optional (nullable)
- [ ] annotatedBy auto-set from authenticated user
- [ ] Non-existent execution → 404
- [ ] Triage on PASS/SKIP execution → allowed (no status restriction)

### Get Triage

- [ ] `GET /test-executions/{eid}/triage` returns triage record if exists
- [ ] Returns `data: null` if no triage yet (not error)
- [ ] Response includes: id, testExecutionId, triageStatus, issueLink, comment, annotatedBy, createdAt, updatedAt

### Triage Summary

- [ ] `GET /triage-summary` returns counts grouped by triageStatus for last 30 days
- [ ] totalTriaged = sum of all categorized items
- [ ] untriaged = count of UNTRIAGED items
- [ ] breakdown map contains all 8 categories
- [ ] Empty project (no triages) → zeros, not error

### Frontend (TriageModal)

- [ ] Click "Triage" button on failed execution → modal opens
- [ ] Dropdown shows all 8 triage categories
- [ ] Current triage status pre-selected (or UNTRIAGED)
- [ ] Issue link text input
- [ ] Comment textarea
- [ ] Save → PUT request, success toast, modal closes
- [ ] Cancel → modal closes, no changes
- [ ] Error on save → error toast, modal stays open
- [ ] Save button shows "Saving..." while request in flight
- [ ] Existing triage data pre-filled when modal opens

### Frontend (Dashboard Widget)

- [ ] TriageBreakdown widget shows 8 categories with counts
- [ ] Categories displayed: New Bug, Known Issue, Script Issue, Data Issue, Env Issue, CR, Other, Untriaged
- [ ] Empty state when no triage data
