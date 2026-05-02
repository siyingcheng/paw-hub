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

---

## Gherkin Scenarios

### Upload JUnit XML

```gherkin
Feature: Upload Test Results

  Scenario: Upload valid JUnit XML via multipart form
    Given project 1 exists
    When I POST /api/v1/projects/1/test-results as multipart/form-data with:
      | field       | value              |
      | file        | valid-junit.xml    |
      | environment | staging            |
    Then the response status is 200
    And a TestRun is created with environment "staging"
    And TestExecutions are created for each test case in the XML

  Scenario: Upload valid XML via JSON body
    Given project 1 exists
    When I POST /api/v1/projects/1/test-results?environment=dev with JSON body containing raw XML
    Then the response status is 200
    And a TestRun is created

  Scenario: Upload without required environment parameter
    When I POST /api/v1/projects/1/test-results with a file but no environment param
    Then the response status is 400

  Scenario: Upload to non-existent project
    When I POST /api/v1/projects/999/test-results with valid XML and environment=dev
    Then the response status is 404

  Scenario: Upload malformed XML
    When I POST /api/v1/projects/1/test-results with "not-valid-xml" and environment=dev
    Then the response status is not 200
    And the response contains an error message

  Scenario: Upload with optional metadata fields
    When I POST /api/v1/projects/1/test-results with:
      | file          | valid-junit.xml |
      | environment   | prod            |
      | runIdentifier | build-42        |
      | branch        | feature/login   |
      | commitSha     | abc123def       |
      | triggeredBy   | ci-bot          |
    Then all metadata fields are stored on the TestRun

  Scenario: Upload with empty test suite
    When I POST /api/v1/projects/1/test-results with XML containing zero test cases
    Then the response status is 200
    And a TestRun is created with totalCases = 0

  Scenario: Run status is FAIL when any test fails
    When I POST with XML containing 5 passed and 1 failed test case
    Then the TestRun status is FAIL

  Scenario: Run status is PASS when all tests pass
    When I POST with XML containing 5 passed and 0 failed test cases
    Then the TestRun status is PASS

  Scenario: Upload returns immediately before analysis completes
    Given analysis takes 2 seconds to complete
    When I POST a valid test result
    Then the response returns in under 500ms
```

### JUnit XML Parsing

```gherkin
Feature: JUnit XML Parsing

  Scenario: Parse standard JUnit XML
    Given XML:
      """
      <testsuite name="AuthTest" tests="3" failures="1" errors="0" skipped="0" time="2.5">
        <testcase name="shouldLogin" classname="com.app.AuthTest" time="1.2"/>
        <testcase name="shouldLogout" classname="com.app.AuthTest" time="0.8"/>
        <testcase name="shouldRefreshToken" classname="com.app.AuthTest" time="0.5">
          <failure message="Expected 200 but got 500" type="AssertionError">
            java.lang.AssertionError at AuthTest.java:42
          </failure>
        </testcase>
      </testsuite>
      """
    When the XML is parsed
    Then 3 executions are created
    And 2 have status PASS, 1 has status FAIL
    And the failed execution has errorMessage and stackTrace populated
    And total duration is approximately 2500ms

  Scenario: Parse XML with error tag
    Given a test case has an <error> tag instead of <failure>
    When the XML is parsed
    Then that execution has status ERROR
    And it is counted as a failure

  Scenario: Parse XML with skipped tag
    Given a test case has a <skipped/> tag
    When the XML is parsed
    Then that execution has status SKIP

  Scenario: Parse multiple test suites
    Given XML contains two <testsuite> elements
    When the XML is parsed
    Then all test cases from both suites are collected
```

### Run List and Detail

```gherkin
Feature: Test Run Browsing

  Scenario: List test runs with pagination
    Given project 1 has 50 test runs
    When I GET /api/v1/projects/1/test-runs?page=0&size=20
    Then the response contains 20 TestRunResponses
    And runs are ordered by createdAt descending

  Scenario: List runs with default pagination
    When I GET /api/v1/projects/1/test-runs without pagination params
    Then page defaults to 0 and size defaults to 20

  Scenario: Get run detail with executions
    Given test run 42 exists in project 1 with 10 executions
    When I GET /api/v1/projects/1/test-runs/42
    Then the response includes run summary fields
    And the response includes all 10 executions ordered by attempt ASC

  Scenario: Get run from different project
    Given test run 42 belongs to project 2
    When I GET /api/v1/projects/1/test-runs/42
    Then the response status is 404

  Scenario: Get non-existent run
    When I GET /api/v1/projects/1/test-runs/99999
    Then the response status is 404
```
