# Analysis Engine

**Priority:** P0 | **Version:** v1

## Overview

Four-step async analysis pipeline triggered after each test result upload. Computes trends, detects flaky tests, clusters failures, and identifies regressions. All steps are idempotent.

## Pipeline Flow

```
TestResultCollectedEvent (async)
  ├── TrendAnalysisService (daily + weekly)
  ├── RegressionDetectionService (run-level + case-level)
  ├── FlakyDetectionService
  └── FailureClusteringService
```

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/v1/projects/{id}/trends` | Yes | Trend data points |
| GET | `/api/v1/projects/{id}/flaky-tests` | Yes | Flaky tests ranked |
| GET | `/api/v1/projects/{id}/failure-clusters` | Yes | Failure clusters |
| GET | `/api/v1/projects/{id}/regressions` | Yes | Regression alerts |

---

## Trend Analysis

### Data Model

```
TrendSnapshot
  id: Long (PK)
  project_id: Long
  date: LocalDate
  environment: String
  periodType: enum (DAILY | WEEKLY)
  passRate: double
  failureRate: double
  avgDurationMs: double
  retryRate: double

  unique: (project_id, date, environment, period_type)
```

### Algorithm

Group TestRun by (project, environment, period) → aggregate averages → upsert TrendSnapshot.

### Acceptance Criteria

- [ ] `GET /trends?period=daily&days=30` returns one data point per day
- [ ] `GET /trends?period=weekly&days=90` returns one data point per week
- [ ] `GET /trends?environment=prod` filters to single environment
- [ ] Daily trend: groups runs by calendar day (UTC)
- [ ] Weekly trend: groups runs by ISO week (Monday start)
- [ ] pass_rate = average of (passed / totalCases) across runs
- [ ] failure_rate = average of (failed / totalCases) across runs
- [ ] avg_duration = average of durationMs across runs
- [ ] Empty day/week with no runs → no data point created
- [ ] Re-running on same day is idempotent (upserts)
- [ ] Invalid period type → error message

---

## Regression Detection

### Algorithm

**Run-level:** Compare today's pass rate against 30-day rolling average. Flag if below (mean − 2×σ).

**Case-level:** For each test case that failed in the latest run, check if it passed N consecutive prior runs (N=5, configurable). If yes → regressed.

### Acceptance Criteria

- [ ] `GET /regressions` returns list of RegressionResponse
- [ ] Run-level: pass_rate < (avg − 2 × σ) → regression flagged
- [ ] Run-level: < 5 historical data points → not checked
- [ ] Run-level: std_dev < 0.005 (0.5%) → too stable, skip
- [ ] Case-level: test passed 5+ consecutive runs, now failed → regressed
- [ ] Case-level: test with < 6 appearances → skipped
- [ ] Response includes: runLevelRegression flag, detail message, regressedCases list
- [ ] Empty list when no regressions detected (not error)

---

## Flaky Test Detection

### Data Model

```
FlakyTestRecord
  id: Long (PK)
  testCaseKey: String (suiteName.className.testName)
  projectId: Long
  flakyScore: double (0.0 ~ 1.0)
  transitionCount: int
  retryPassCount: int
  lastDetectedAt: Instant
```

### Algorithm

Per test_case_key over last N days (configurable, default 30):
- Count PASS ↔ FAIL transitions between consecutive runs
- flaky_score = transitions / total_appearances
- Bonus: + (retry_pass_count / total_appearances) × 0.3
- Cap at 1.0
- Score ≥ threshold (default 0.3) → record

### Acceptance Criteria

- [ ] `GET /flaky-tests` returns list sorted by flakyScore DESC
- [ ] Consistently passing test → score 0
- [ ] Alternating PASS/FAIL → high score
- [ ] Retry-then-pass pattern → elevated score
- [ ] Test with < 3 appearances in window → excluded
- [ ] Response includes: testCaseKey, flakyScore, transitionCount, retryPassCount, lastDetectedAt
- [ ] Score thresholds configurable via application.yml

---

## Failure Clustering

### Data Model

```
FailureCluster
  id: Long (PK)
  projectId: Long
  clusterKey: String (SHA-256 hash of normalized error)
  representativeError: String (most frequent variant)
  occurrenceCount: int
  firstSeen: Instant
  lastSeen: Instant
```

### Algorithm

1. Filter: executions with non-empty errorMessage
2. Normalize: strip numbers → "0", hex values → "0xHEX", UUIDs → "UUID"
3. Hash: SHA-256(normalized_message)
4. Group by hash
5. Representative: most frequent original message in group
6. Upsert FailureCluster

### Acceptance Criteria

- [ ] `GET /failure-clusters` returns list sorted by occurrenceCount DESC
- [ ] Same error with different timestamps → same cluster
- [ ] Same error with different line numbers (123 vs 456) → same cluster
- [ ] Different error types (NPE vs TimeoutException) → different clusters
- [ ] Most frequent original message chosen as representative
- [ ] occurrenceCount reflects number of grouped failures
- [ ] firstSeen set on first occurrence, not overwritten
- [ ] lastSeen updated on every occurrence
- [ ] Empty result when no failures (not error)

---

## Configurable Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `analysis.flaky-threshold` | 0.3 | Minimum flaky score |
| `analysis.regression-sigma` | 2.0 | Sigma multiplier for run-level regression |
| `analysis.window-days` | 30 | Analysis lookback window |
| `analysis.consecutive-pass-count` | 5 | Passes before a failure = case-level regression |

---

## Gherkin Scenarios

### Trend Analysis

```gherkin
Feature: Trend Analysis

  Scenario: Get daily trends for 30 days
    Given project 1 has test runs over the last 30 days in "prod" environment
    When I GET /api/v1/projects/1/trends?period=daily&days=30
    Then the response contains one TrendResponse per environment
    And each TrendResponse has up to 30 data points

  Scenario: Get weekly trends for 90 days
    Given project 1 has test runs over the last 90 days
    When I GET /api/v1/projects/1/trends?period=weekly&days=90
    Then data points are grouped by ISO week

  Scenario: Filter trends by environment
    When I GET /api/v1/projects/1/trends?environment=prod
    Then only "prod" environment data is returned

  Scenario: Empty day has no data point
    Given no test runs exist on 2026-04-15
    When I query daily trends for that period
    Then no data point exists for 2026-04-15

  Scenario: Re-computing trend is idempotent
    Given a daily trend for 2026-05-01 already exists
    When analysis runs again for the same day
    Then the existing TrendSnapshot is updated (upserted), not duplicated

  Scenario: Invalid period type returns error
    When I GET /api/v1/projects/1/trends?period=hourly
    Then the response contains an error message

  Scenario: Trend metrics are computed correctly
    Given 3 test runs in "dev" on 2026-05-01:
      | run | passed | total | duration |
      | 1   | 8      | 10    | 5000ms   |
      | 2   | 9      | 10    | 6000ms   |
      | 3   | 7      | 10    | 4000ms   |
    When daily trend is computed for 2026-05-01
    Then pass_rate = (0.8 + 0.9 + 0.7) / 3 = 0.80
    And avg_duration = (5000 + 6000 + 4000) / 3 = 5000ms
```

### Regression Detection

```gherkin
Feature: Regression Detection

  Scenario: Run-level regression detected
    Given the last 30 days have average pass rate 0.92 with std_dev 0.03
    And today's pass rate is 0.82
    When regression detection runs
    Then runLevelRegression is true
    And runLevelDetail explains the deviation

  Scenario: Run-level regression not flagged when within sigma
    Given the last 30 days have average pass rate 0.92 with std_dev 0.05
    And today's pass rate is 0.88
    When regression detection runs
    Then runLevelRegression is false

  Scenario: Insufficient history skipped
    Given only 3 historical data points exist
    When regression detection runs
    Then run-level regression is not checked

  Scenario: Stable dataset skipped
    Given historical pass rates have std_dev < 0.005
    When regression detection runs
    Then run-level regression is not checked

  Scenario: Case-level regression detected
    Given test "shouldLogin" passed in the last 6 consecutive runs
    And test "shouldLogin" failed in the latest run
    When regression detection runs
    Then "shouldLogin" appears in regressedCases list

  Scenario: Case-level regression with insufficient history
    Given test "newTest" appeared in only 3 runs
    When regression detection runs
    Then "newTest" is skipped for case-level detection

  Scenario: Empty regressions list when none detected
    Given all tests are stable
    When I GET /api/v1/projects/1/regressions
    Then the response is a valid list (not an error)
    And the list may be empty
```

### Flaky Test Detection

```gherkin
Feature: Flaky Test Detection

  Scenario: Consistently passing test has score 0
    Given test "stableTest" passed in all 10 runs in the last 30 days
    When flaky detection runs
    Then the flaky score for "stableTest" is 0
    And it does not appear in the flaky tests list

  Scenario: Alternating PASS/FAIL gives high score
    Given test "unstableTest" alternates PASS, FAIL, PASS, FAIL, PASS, FAIL over 6 runs
    When flaky detection runs
    Then the flaky score is at least 0.8
    And it appears in the flaky tests list sorted by score descending

  Scenario: Retry-then-pass pattern elevates score
    Given test "retryTest" failed on attempt 1 and passed on attempt 2 in 3 out of 5 runs
    When flaky detection runs
    Then the flaky score is elevated compared to a test with no retries

  Scenario: Test with fewer than 3 appearances excluded
    Given test "rareTest" appeared in only 2 runs in the last 30 days
    When flaky detection runs
    Then "rareTest" is excluded from flaky analysis

  Scenario: Flaky score never exceeds 1.0
    Given test "extremelyFlaky" has high transition and retry counts
    When flaky detection runs
    Then the flaky score is capped at 1.0

  Scenario: Threshold configuration respected
    Given flaky-threshold is set to 0.5
    When a test has flaky score 0.4
    Then it does not appear in flaky tests results
```

### Failure Clustering

```gherkin
Feature: Failure Clustering

  Scenario: Same error clusters together
    Given 5 executions failed with "NullPointerException at AuthService.java:42"
    And 3 executions failed with "NullPointerException at AuthService.java:156"
    When failure clustering runs
    Then both groups map to the same cluster (normalized line numbers)
    And the cluster occurrenceCount is 8

  Scenario: Different error types form separate clusters
    Given 5 executions failed with "NullPointerException"
    And 5 executions failed with "TimeoutException"
    When failure clustering runs
    Then two separate clusters are created

  Scenario: Representative error is most frequent variant
    Given a cluster has 10 occurrences of "Connection refused to db.internal:5432"
    And 2 occurrences of "Connection refused to db.internal:5433"
    When failure clustering runs
    Then the representativeError is "Connection refused to db.internal:5432"

  Scenario: firstSeen preserved on subsequent runs
    Given a failure cluster exists with firstSeen = 2026-04-01
    When a new failure matches this cluster on 2026-05-01
    Then firstSeen remains 2026-04-01
    And lastSeen is updated to 2026-05-01

  Scenario: Empty failures returns no error
    Given there are no failed executions in the analysis window
    When I GET /api/v1/projects/1/failure-clusters
    Then the response is a valid empty list (not an error)
```
