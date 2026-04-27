# Reporting

**Priority:** P2 | **Version:** v1

## Overview

Time-range summary with top failure identification and JSON export. Provides a quick overview of project health over a configurable period.

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/v1/projects/{id}/summary` | Yes | Time-range summary |
| GET | `/api/v1/projects/{id}/export` | Yes | JSON export |

## Acceptance Criteria

### Summary

- [ ] `GET /summary?days=30` returns summary for last 30 days
- [ ] `days` default is 30
- [ ] Response includes: totalRuns, overallPassRate, totalFailures
- [ ] Response includes topFailure: testName, errorMessage, failCount
- [ ] topFailure is the most frequently failing test case in the period
- [ ] topFailure is null when no failures
- [ ] Response includes topFlakyTests list

### Export

- [ ] `GET /export?format=json` returns pretty-printed JSON summary
- [ ] `GET /export?format=pdf` → error "Only JSON export supported in v1"
- [ ] Response Content-Type: application/json
- [ ] Export data matches summary endpoint data

## Out of Scope (v1)

- PDF export
- CSV export
- Scheduled/cron exports
- Email delivery
