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
