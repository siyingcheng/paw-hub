# Paw-Hub v1 Requirements

**Date:** 2026-04-27
**Priority:** P0 = Must have, P1 = Should have, P2 = Nice to have

---

## P0 — Core Data Pipeline

### R1: Test Result Collection

CI systems upload JUnit XML (or JSON) via REST API. The system parses, validates, and stores results.

**Backend:**
- `POST /api/v1/projects/{id}/test-results` accepts `multipart/form-data` (file) and `application/json` (body)
- Required params: `environment` (dev/staging/prod). Optional: `runIdentifier`, `branch`, `commitSha`, `triggeredBy`
- Response: `{ success, data: { id, projectId, totalCases, passed, failed, ... } }`

**Acceptance:**
- [ ] Upload valid JUnit XML → 200, test run created, executions stored
- [ ] Upload empty XML → 200, test run with 0 cases
- [ ] Upload malformed XML → error message
- [ ] Upload with missing `environment` param → 400
- [ ] Upload to non-existent project → 404
- [ ] `<failure>` tag parsed correctly (message, type, stack trace)
- [ ] `<error>` tag parsed as ERROR status
- [ ] `<skipped>` tag parsed as SKIP status
- [ ] Test run counts (total/passed/failed/skipped) match XML summary
- [ ] Duration correctly converted from seconds (XML) to milliseconds (stored)

### R2: Test Run Browsing

QA can browse historical test runs and drill into details.

**Backend:**
- `GET /api/v1/projects/{id}/test-runs` — paginated list (page, size params), ordered by newest first
- `GET /api/v1/projects/{id}/test-runs/{runId}` — single run with all executions

**Acceptance:**
- [ ] List returns runs in descending date order
- [ ] List supports pagination (page=0&size=20)
- [ ] Run detail includes all TestExecution records
- [ ] Run detail includes summary (passed/failed/skipped/duration/env)
- [ ] Non-existent run ID → 404

### R3: Basic Statistics

Per-run statistics computed during ingestion: pass rate, failure rate, duration, retry rate.

**Backend:**
- Computed during `CollectionService.ingest()` from parsed JUnit XML
- Stored on TestRun entity

**Acceptance:**
- [ ] Pass rate = final-attempt passed / total cases
- [ ] Failure rate = final-attempt failed / total cases
- [ ] Duration = sum of all execution durations
- [ ] Run status = FAIL if any failure, PASS otherwise

---

## P0 — Analysis Engine

### R4: Trend Analysis

Daily and weekly aggregated statistics per project per environment.

**Backend:**
- `GET /api/v1/projects/{id}/trends?period=daily|weekly&days=30&environment=prod`
- Computed asynchronously after each test result upload
- Each data point: pass_rate, failure_rate, avg_duration, retry_rate

**Acceptance:**
- [ ] Daily trend returns one data point per day for the period
- [ ] Weekly trend returns one data point per week for the period
- [ ] Trend data filtered by environment when specified
- [ ] Trend data returns all environments when not specified
- [ ] Empty result when no data in range (not error)
- [ ] Invalid period type → error message
- [ ] Trend computation is idempotent (re-running doesn't duplicate)
- [ ] Trend runs asynchronously (upload returns before trend is computed)

### R5: Flaky Test Detection

Detect tests that alternate between PASS and FAIL across runs.

**Backend:**
- `GET /api/v1/projects/{id}/flaky-tests` — ranked by flaky score descending
- Algorithm: transitions / total_appearances + retry_weight × 0.3
- Threshold: score >= 0.3 (configurable) → flaky
- Window: last 30 days (configurable)
- Retry-pass cases weighted higher

**Acceptance:**
- [ ] Test with consistent PASS → score = 0
- [ ] Test with alternating PASS/FAIL → high score
- [ ] Test with retry-then-pass pattern → elevated score
- [ ] Test with < 3 appearances → excluded
- [ ] List sorted by flaky_score descending
- [ ] Response includes score, transition_count, retry_pass_count, last_detected

### R6: Failure Clustering

Group similar failures by normalizing and hashing error messages.

**Backend:**
- `GET /api/v1/projects/{id}/failure-clusters` — ranked by occurrence count descending
- Normalization: strip numbers, hex values, UUIDs
- Hash normalized message → cluster_key
- Representative error = most frequent variant in cluster

**Acceptance:**
- [ ] Same error with different timestamps → same cluster
- [ ] Same error with different IDs/line numbers → same cluster
- [ ] Different error types → different clusters
- [ ] Most frequent variant chosen as representative
- [ ] Empty result when no failures (not error)
- [ ] Cluster occurrence_count updated on new failures

### R7: Regression Detection

Detect when pass rate drops significantly or a previously-stable test starts failing.

**Backend:**
- `GET /api/v1/projects/{id}/regressions`
- Run-level: pass_rate < (rolling_30d_avg - 2×σ)
- Case-level: test passed N consecutive runs then failed (N=5, configurable)

**Acceptance:**
- [ ] Run-level triggered when pass rate drops > 2 sigma below mean
- [ ] Run-level NOT triggered when < 5 historical data points
- [ ] Run-level NOT triggered when std_dev < 0.5% (too stable to be meaningful)
- [ ] Case-level triggered when test passes 5+ consecutive runs then fails
- [ ] Case-level NOT triggered for tests with < 6 total appearances
- [ ] Response includes runLevelRegression flag, detail message, regressedCases list
- [ ] Regression detection runs asynchronously after upload

---

## P1 — Failure Triage

### R8: Failure Annotation

QA can annotate failed test cases with failure category, issue link, and comments.

**Backend:**
- `PUT /api/v1/projects/{id}/test-executions/{eid}/triage` — create or update triage
- `GET /api/v1/projects/{id}/test-executions/{eid}/triage` — get triage
- `GET /api/v1/projects/{id}/triage-summary` — aggregated stats

**Triage categories:** UNTRIAGED, NEW_BUG, KNOWN_ISSUE, SCRIPT_ISSUE, DATA_ISSUE, ENV_ISSUE, CR, OTHER

**Frontend:**
- Triage modal on each failed execution in Test Explorer
- Dropdown for category, text fields for issue link and comment

**Acceptance:**
- [ ] Save triage on failed execution → 200, triage record created
- [ ] Update existing triage → 200, fields updated, updated_at changed
- [ ] Triage on non-existent execution → 404
- [ ] Triage on non-failed execution → allowed (status field is separate)
- [ ] Triage summary returns counts by category for last 30 days
- [ ] Triage summary includes untriaged count
- [ ] Dashboard shows triage breakdown widget
- [ ] Modal shows success toast on save
- [ ] Modal shows error toast on failure

---

## P1 — Auth & Multi-Tenancy

### R9: User Authentication

JWT-based login and registration.

**Backend:**
- `POST /api/v1/auth/register` — create account (username, email, password)
- `POST /api/v1/auth/login` — returns JWT token
- `GET /api/v1/auth/me` — current user info + memberships

**Acceptance:**
- [ ] Register with unique username → 200, token returned
- [ ] Register with duplicate username → 409
- [ ] Login with correct credentials → 200, token returned
- [ ] Login with wrong password → 401
- [ ] Login with non-existent user → 401
- [ ] Protected endpoints reject requests without token → 401/403
- [ ] Protected endpoints accept valid token → 200
- [ ] Expired token → 401

### R10: Multi-Tenancy & Role-Based Access

Organization → Team → Project hierarchy with role-based access.

**Roles:** ADMIN, QA, VIEWER

**Backend:**
- `GET /api/v1/projects/{id}/my-role` — current user's role in project

**Acceptance:**
- [ ] ADMIN can access Settings page (frontend)
- [ ] QA cannot see Settings link in sidebar
- [ ] VIEWER cannot see Settings link in sidebar
- [ ] Settings page redirects non-ADMIN to dashboard
- [ ] /my-role returns "NONE" for user not in project's team
- [ ] Role badge displayed in sidebar

---

## P2 — Reporting & Export

### R11: Summary & Export

Time-range summary with top failures and JSON export.

**Backend:**
- `GET /api/v1/projects/{id}/summary?days=30`
- `GET /api/v1/projects/{id}/export?format=json&days=30`

**Acceptance:**
- [ ] Summary includes totalFailures count
- [ ] Summary includes top failure (most frequent)
- [ ] Export returns pretty-printed JSON
- [ ] Non-JSON format → error message

---

## P2 — Frontend UX

### R12: Dashboard

Project overview with KPIs and charts.

**Frontend:** `/projects/{id}`
**Components:** KPI cards, PassRateChart, RecentRegressions, TriageBreakdown

**Acceptance:**
- [ ] KPI cards show: latest pass rate, flaky count, untriaged, total triaged
- [ ] Pass rate chart shows daily trend for dev/staging/prod (3 lines)
- [ ] RecentRegressions shows run-level and case-level regressions
- [ ] TriageBreakdown shows failure count by category
- [ ] Loading spinner shown while data loads
- [ ] Error toast shown on API failure

### R13: Test Explorer

Drill into a single test run with filtering and triage.

**Frontend:** `/projects/{id}/runs/{runId}`
**Components:** FilterBar, TestTable, TriageModal, ErrorDetail

**Acceptance:**
- [ ] Run summary bar shows passed/failed/skipped/duration/env/branch
- [ ] Search filters by test name or class name
- [ ] Status filter filters by PASS/FAIL/SKIP/ERROR
- [ ] Failed executions have clickable "Triage" button
- [ ] Triage modal has category dropdown, issue link, comment fields
- [ ] Error detail expandable for failed cases
- [ ] Retry attempt number shown for each execution
- [ ] Loading spinner while data loads

### R14: Trends & Analysis

Detailed analysis view with charts and tables.

**Frontend:** `/projects/{id}/trends`
**Components:** PassRateChart, FlakyTable, ClusterTable

**Acceptance:**
- [ ] Pass rate trend chart displayed
- [ ] Flaky tests table with score, transition count
- [ ] Failure clusters table with representative error, count
- [ ] Empty states shown when no data

### R15: Toast Notifications

User feedback for all actions.

**Acceptance:**
- [ ] Success toast (green) shown on successful actions
- [ ] Error toast (red) shown on failures
- [ ] Toasts auto-dismiss after 5 seconds
- [ ] Toasts manually dismissible via X button
- [ ] Slide-in animation

---

## Out of Scope (v1)

- Email/webhook notifications
- PDF export (JSON export only)
- SSO / OAuth integration
- Test comparison across branches (diff view)
- Custom dashboard widgets
- Tenant CRUD management UI (backend service exists, no UI)
- Retry upload (re-submitting failed cases within same run)
