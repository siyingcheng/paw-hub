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

---

## Gherkin Scenarios

### Summary

```gherkin
Feature: Project Summary

  Scenario: Get summary for last 30 days
    Given project 1 has test runs and executions over the last 30 days
    When I GET /api/v1/projects/1/summary?days=30
    Then the response status is 200
    And response includes totalRuns, overallPassRate, and totalFailures

  Scenario: Default days parameter
    When I GET /api/v1/projects/1/summary without specifying days
    Then the response uses a default of 30 days

  Scenario: Top failure is most frequently failing test
    Given in the last 30 days, "shouldLogin" failed 15 times
    And "searchUsers" failed 8 times
    When I GET /api/v1/projects/1/summary
    Then topFailure.testName is "shouldLogin"
    And topFailure.failCount is 15

  Scenario: No top failure when all tests pass
    Given project 2 has zero failures in the last 30 days
    When I GET /api/v1/projects/2/summary
    Then topFailure is null

  Scenario: Summary includes top flaky tests
    Given project 1 has flaky test records
    When I GET /api/v1/projects/1/summary
    Then response includes a topFlakyTests list
```

### Export

```gherkin
Feature: Data Export

  Scenario: Export summary as JSON
    When I GET /api/v1/projects/1/export?format=json&days=30
    Then the response status is 200
    And the Content-Type is application/json
    And the response body is a pretty-printed JSON summary
    And the exported data matches the /summary endpoint data

  Scenario: Reject PDF export
    When I GET /api/v1/projects/1/export?format=pdf
    Then the response contains an error message
    And the message indicates only JSON export is supported in v1

  Scenario: Reject unsupported export format
    When I GET /api/v1/projects/1/export?format=csv
    Then the response contains an error message
```
