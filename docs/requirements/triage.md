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

---

## Gherkin Scenarios

### Save Triage

```gherkin
Feature: Save Triage

  Scenario: Create first triage on execution
    Given test execution 100 has FAIL status and no existing triage
    When I PUT /api/v1/projects/1/test-executions/100/triage with
         {"triageStatus": "NEW_BUG", "issueLink": "https://jira.example.com/PROJ-123"}
    Then the response status is 200
    And a FailureTriage record is created
    And annotatedBy is set to my user ID
    And createdAt is set to now

  Scenario: Update existing triage
    Given test execution 100 already has triage status "NEW_BUG"
    When I PUT /api/v1/projects/1/test-executions/100/triage with
         {"triageStatus": "KNOWN_ISSUE", "comment": "Duplicate of PROJ-99"}
    Then the response status is 200
    And triageStatus is updated to "KNOWN_ISSUE"
    And comment is updated to "Duplicate of PROJ-99"
    And updatedAt is refreshed

  Scenario: Triage with missing required status
    When I PUT /api/v1/projects/1/test-executions/100/triage with
         {"issueLink": "https://jira.example.com/PROJ-123"}
    Then the response status is 400
    And the response message indicates triageStatus is required

  Scenario: Triage on non-existent execution
    When I PUT /api/v1/projects/1/test-executions/99999/triage with
         {"triageStatus": "NEW_BUG"}
    Then the response status is 404

  Scenario: Triage on PASS execution is allowed
    Given test execution 200 has status PASS
    When I PUT /api/v1/projects/1/test-executions/200/triage with
         {"triageStatus": "OTHER", "comment": "Reviewed, no issue"}
    Then the response status is 200

  Scenario: Optional fields can be null
    When I PUT /api/v1/projects/1/test-executions/100/triage with
         {"triageStatus": "ENV_ISSUE"}
    Then the response status is 200
    And issueLink is null
    And comment is null
```

### Get Triage

```gherkin
Feature: Get Triage

  Scenario: Get existing triage
    Given test execution 100 has a triage with status "NEW_BUG"
    When I GET /api/v1/projects/1/test-executions/100/triage
    Then the response status is 200
    And response includes id, testExecutionId, triageStatus, issueLink, comment,
         annotatedBy, createdAt, updatedAt

  Scenario: Get triage for untriaged execution
    Given test execution 300 has no triage record
    When I GET /api/v1/projects/1/test-executions/300/triage
    Then the response status is 200
    And response data is null
```

### Triage Summary

```gherkin
Feature: Triage Summary

  Scenario: Get triage summary with data
    Given in the last 30 days:
      | status        | count |
      | UNTRIAGED     | 15    |
      | NEW_BUG       | 8     |
      | KNOWN_ISSUE   | 5     |
      | SCRIPT_ISSUE  | 3     |
      | ENV_ISSUE     | 2     |
      | OTHER         | 1     |
      | DATA_ISSUE    | 0     |
      | CR            | 0     |
    When I GET /api/v1/projects/1/triage-summary
    Then totalTriaged is 19 (NEW_BUG + KNOWN_ISSUE + SCRIPT_ISSUE + ENV_ISSUE + OTHER)
    And untriaged is 15
    And breakdown map has all 8 categories with correct counts

  Scenario: Empty project returns zeros
    Given project 2 has no triage records
    When I GET /api/v1/projects/2/triage-summary
    Then totalTriaged is 0
    And untriaged is 0
    And all breakdown values are 0
```

### Frontend: TriageModal

```gherkin
Feature: Triage Modal

  Scenario: Open triage modal on untriaged execution
    Given I am viewing a failed test execution with no triage
    When I click the "Triage" button
    Then a modal opens
    And the triage status dropdown shows all 8 categories
    And "UNTRIAGED" is pre-selected
    And issue link and comment fields are empty

  Scenario: Open triage modal with existing triage
    Given I am viewing a failed test execution with triage status "KNOWN_ISSUE"
    And an issue link "https://jira.example.com/PROJ-50" exists
    When I click the "Triage" button
    Then the modal opens
    And "KNOWN_ISSUE" is pre-selected in the dropdown
    And the issue link is pre-filled
    And existing comment is pre-filled

  Scenario: Save triage successfully
    Given the triage modal is open on execution 100
    When I select "NEW_BUG", enter issue link and comment, and click Save
    Then a PUT request is sent
    And the modal closes
    And a green success toast appears

  Scenario: Save triage fails
    Given the triage modal is open
    When I click Save and the API returns an error
    Then a red error toast appears
    And the modal stays open

  Scenario: Cancel triage modal
    Given the triage modal is open
    When I click Cancel
    Then the modal closes
    And no PUT request is sent

  Scenario: Save button shows loading state
    Given the triage modal is open
    When I click Save
    Then the button text changes to "Saving..."
    And the button is disabled during the request
```

### Frontend: Triage Breakdown Widget

```gherkin
Feature: Triage Breakdown Widget

  Scenario: Show triage breakdown on dashboard
    Given project 1 has triage data across multiple categories
    When I view the dashboard at /projects/1
    Then I see a TriageBreakdown widget
    And it shows 8 categories: New Bug, Known Issue, Script Issue, Data Issue,
         Env Issue, CR, Other, Untriaged
    And each category shows its count

  Scenario: Empty triage breakdown
    Given project 1 has no triage data
    When I view the dashboard at /projects/1
    Then the TriageBreakdown widget shows an empty state
```
