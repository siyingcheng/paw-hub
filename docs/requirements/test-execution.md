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

---

## Gherkin Scenarios

### Data Integrity

```gherkin
Feature: Test Execution Data Integrity

  Scenario: Create execution with valid data
    Given a TestRun exists with id 1
    When a TestExecution is created with suiteName "AuthTest", className "com.app.AuthTest",
         testName "shouldLogin", status PASS, duration 1200ms
    Then the execution is persisted with attempt = 1
    And status is "PASS"
    And error fields are null

  Scenario: Duplicate execution is rejected
    Given an execution exists for (run=1, suite="AuthTest", class="com.app.AuthTest", name="shouldLogin", attempt=1)
    When another execution with the same key is inserted
    Then a unique constraint violation occurs

  Scenario: Execution with FAIL status stores error details
    Given a TestRun exists with id 1
    When a TestExecution is created with status FAIL, errorMessage "Expected 200 but got 500",
         errorType "AssertionError", and stack trace
    Then all error fields are persisted

  Scenario: caseNumber is optional
    Given a TestRun exists with id 1
    When a TestExecution is created without a caseNumber
    Then the execution is persisted with caseNumber = null
```

### Retry Handling

```gherkin
Feature: Retry Handling

  Scenario: Multiple attempts for same test case
    Given a TestRun exists with id 1
    And an execution exists with (run=1, suite="ApiTest", class="com.app.ApiTest", name="searchUsers", attempt=1, status=FAIL)
    When a retry execution is created with (run=1, suite="ApiTest", class="com.app.ApiTest", name="searchUsers", attempt=2, status=PASS)
    Then both executions exist
    And the final-attempt status for "searchUsers" is PASS

  Scenario: Retry-pass pattern detected by flaky analysis
    Given a test case has attempt=1 with FAIL and attempt=2 with PASS
    When flaky analysis runs
    Then the retry_pass_count for this test case is 1
    And the flaky score is elevated

  Scenario: All attempts contribute to total duration
    Given a test case has attempt=1 with 500ms and attempt=2 with 300ms
    Then the total cost for this test case is 800ms
```

### Frontend Display

```gherkin
Feature: Test Explorer Page

  Scenario: View test run executions
    Given I am on /projects/1/runs/42
    When the page loads
    Then I see a summary bar with passed, failed, skipped counts, duration, environment, and branch
    And I see a table with all executions

  Scenario: Filter executions by search
    Given I am viewing run 42 with 100 executions
    When I type "login" in the search input
    Then only executions whose testName or className contains "login" are shown

  Scenario: Filter executions by status
    Given I am viewing run 42
    When I select "FAIL" from the status dropdown
    Then only executions with status FAIL or ERROR are shown

  Scenario: Expand error detail on failed execution
    Given I am viewing run 42 with a failed execution
    When I click "Details" on the failed row
    Then the error message, error type, and stack trace are displayed
    And the content is in a scrollable code block

  Scenario: Triage button on failed execution
    Given I am viewing run 42 with a failed execution
    When I click the "Triage" button
    Then the TriageModal opens for that execution

  Scenario: Status color coding
    Given I am viewing run 42
    Then PASS executions display a green badge
    And FAIL/ERROR executions display a red badge
    And SKIP executions display a yellow badge
```
