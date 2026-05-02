# Requirements Verification Checklist

Verify in order — each section depends on the ones before it.
Source files in `docs/requirements/` contain full context.

---

## 1. User Management

Source: `docs/requirements/user-management.md` (P1)

Foundation. Users, authentication, JWT tokens, and role-based access. Nothing else works without this.

### Registration

- [ ] Register with unique username and valid email → 200, token returned `{ token, userId, username }`
- [ ] Register with duplicate username → 409, "Username already taken"
- [ ] Register with missing/invalid email → 400
- [ ] Password is BCrypt-hashed before storage (never plaintext)

### Login

- [ ] Login with correct credentials → 200, JWT token returned
- [ ] Login with wrong password → 401, "Invalid credentials"
- [ ] Login with non-existent username → 401, "Invalid credentials"
- [ ] Token contains: userId (sub), username (claim), issuedAt, expiration
- [ ] Token expires after configured duration (default: 24h)

### Auth Filter

- [ ] Requests without Authorization header → continue without auth (handled by security config)
- [ ] Requests with `Bearer <valid_token>` → userId set in SecurityContext
- [ ] Requests with `Bearer <invalid_token>` → continue without auth (no crash)
- [ ] Requests with `Bearer <expired_token>` → continue without auth

### Current User

- [ ] `GET /auth/me` returns user id, username, email, and all memberships with roles
- [ ] Unauthenticated request → 401/403

### Frontend: Login page (`/login`)

- [ ] Username and password inputs
- [ ] Submit calls `POST /auth/login`
- [ ] Success: JWT stored, redirect to `/projects/1`
- [ ] Success: green toast "Welcome, {username}"
- [ ] Failure: red toast with error message
- [ ] Button shows "Logging in..." while loading, disabled
- [ ] Link to Register page

### Frontend: Register page (`/register`)

- [ ] Username, email, password inputs
- [ ] Submit calls `POST /auth/register`
- [ ] Success: green toast, redirect to `/login`
- [ ] Failure: red toast with error message
- [ ] Button shows "Registering..." while loading, disabled
- [ ] Link to Login page

### Frontend: Navbar

- [ ] "Paw-Hub" branding + project name
- [ ] Logout button (clears JWT, redirects to /login)

### Frontend: Role-based sidebar & access

- [ ] ADMIN sees Sidebar with: Dashboard, Trends, Settings
- [ ] QA sees Sidebar with: Dashboard, Trends (no Settings)
- [ ] VIEWER sees Sidebar with: Dashboard, Trends (no Settings)
- [ ] Settings page redirects non-ADMIN to dashboard with error toast
- [ ] Role badge shown in sidebar footer (ADMIN/QA/VIEWER)
- [ ] User registers → has no team membership (must be added by admin)

---

## 2. Project Management (Multi-Tenancy)

Source: `docs/requirements/project-management.md` (P1)

Organization → Team → Project hierarchy. Projects are the container for all test data.

### Project Info

- [ ] `GET /projects/{id}` returns project name, apiKey, team name, org name
- [ ] Non-existent project → 404
- [ ] API key format: `sk-proj-` + 20 random chars

### Role Query

- [ ] `GET /projects/{id}/my-role` returns role for current user in project's team
- [ ] User not in team → returns "NONE"
- [ ] User is ADMIN → returns "ADMIN"
- [ ] User is QA → returns "QA"

### Tenant Service (backend only, no UI in v1)

- [ ] `createOrg(name)` → Organization persisted
- [ ] `createTeam(orgId, name)` → Team persisted, linked to Org
- [ ] `createProject(teamId, name, userId)` → Project persisted, auto-generates apiKey
- [ ] `createProject` fails if user is not team member → 403
- [ ] `addMember(teamId, userId, role)` → Membership created
- [ ] `addMember` fails if already a member → 409

---

## 3. Test Run

Source: `docs/requirements/test-run.md` (P0)

Data entry point. CI pipelines upload JUnit XML; the system parses it and creates test runs.

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

---

## 4. Test Execution

Source: `docs/requirements/test-execution.md` (P0)

Individual test case results within a run. Supports retries (multiple attempts per case).

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

### Frontend: Test Explorer page (`/projects/{id}/runs/{runId}`)

- [ ] Run summary bar: passed/failed/skipped counts, duration, environment, branch
- [ ] Each execution shown as a table row
- [ ] Columns: #, Case ID, Suite/Class, Test Name, Attempt, Status, Duration, Error, Triage
- [ ] Status color-coded: PASS=green, FAIL/ERROR=red, SKIP=yellow
- [ ] Failed executions have expandable error detail (message + stack trace)
- [ ] Failed executions have "Triage" button → opens TriageModal
- [ ] Search input filters by testName or className (case-insensitive)
- [ ] Status dropdown filters by PASS/FAIL/SKIP/ERROR

---

## 5. Analysis Engine

Source: `docs/requirements/analysis.md` (P0)

Async pipeline triggered after each upload. Four independent analysis steps, all idempotent.

### Trend Analysis

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

### Regression Detection

- [ ] `GET /regressions` returns list of RegressionResponse
- [ ] Run-level: pass_rate < (avg − 2 × σ) → regression flagged
- [ ] Run-level: < 5 historical data points → not checked
- [ ] Run-level: std_dev < 0.005 (0.5%) → too stable, skip
- [ ] Case-level: test passed 5+ consecutive runs, now failed → regressed
- [ ] Case-level: test with < 6 appearances → skipped
- [ ] Response includes: runLevelRegression flag, detail message, regressedCases list
- [ ] Empty list when no regressions detected (not error)

### Flaky Test Detection

- [ ] `GET /flaky-tests` returns list sorted by flakyScore DESC
- [ ] Consistently passing test → score 0
- [ ] Alternating PASS/FAIL → high score
- [ ] Retry-then-pass pattern → elevated score
- [ ] Test with < 3 appearances in window → excluded
- [ ] Response includes: testCaseKey, flakyScore, transitionCount, retryPassCount, lastDetectedAt
- [ ] Score thresholds configurable via application.yml

### Failure Clustering

- [ ] `GET /failure-clusters` returns list sorted by occurrenceCount DESC
- [ ] Same error with different timestamps → same cluster
- [ ] Same error with different line numbers (123 vs 456) → same cluster
- [ ] Different error types (NPE vs TimeoutException) → different clusters
- [ ] Most frequent original message chosen as representative
- [ ] occurrenceCount reflects number of grouped failures
- [ ] firstSeen set on first occurrence, not overwritten
- [ ] lastSeen updated on every occurrence
- [ ] Empty result when no failures (not error)

### Frontend: Trends page (`/projects/{id}/trends`)

- [ ] PassRateChart: Recharts LineChart with dev/staging/prod lines, tooltip, legend, grid
- [ ] Y-axis auto-scaled (not hardcoded)
- [ ] Flaky tests table: test case key, score, transition count
- [ ] Failure clusters table: representative error, occurrence count
- [ ] Empty states when no data
- [ ] Loading spinner while data loads

---

## 6. Failure Triage

Source: `docs/requirements/triage.md` (P1)

QA engineers annotate failed executions with a failure category, issue link, and comment.

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

### Frontend: TriageModal

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

### Frontend: TriageBreakdown widget (Dashboard)

- [ ] Shows 8 categories with counts
- [ ] Categories: New Bug, Known Issue, Script Issue, Data Issue, Env Issue, CR, Other, Untriaged
- [ ] Empty state when no triage data

---

## 7. Reporting

Source: `docs/requirements/reporting.md` (P2)

Time-range summary with top failure identification and JSON export.

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

---

## 8. Frontend — Shared Infrastructure

Source: `docs/requirements/frontend.md` (P2)

Cross-cutting UI: shared components, API client, toast system, and the Dashboard and Settings pages.

### Dashboard page (`/projects/{id}`)

- [ ] KPI cards: Latest Pass Rate, Flaky Tests count, Untriaged count, Total Triaged
- [ ] PassRateChart (shared, already verified in Trends)
- [ ] RecentRegressions: run-level alerts + case-level regressed tests
- [ ] TriageBreakdown (already verified in Triage)
- [ ] Sidebar with role-based links (already verified in User Management)
- [ ] Navbar with project name + logout (already verified in User Management)
- [ ] Loading spinner while data loads
- [ ] Error toast on API failure

### Settings page (`/projects/{id}/settings`)

- [ ] Non-ADMIN redirected to dashboard with error toast (already verified in User Management)
- [ ] Project info: name, org, team, API key (with code styling)
- [ ] Upload endpoint shown: `POST /api/v1/projects/{id}/test-results`
- [ ] Analysis thresholds: flaky, regression sigma, window
- [ ] Team members list with role badges

### FilterBar component

- [ ] Search input (filters by test name and class name, case-insensitive)
- [ ] Status dropdown (All, PASS, FAIL, SKIP, ERROR)

### ErrorDetail component

- [ ] Expandable "Details" button on failed executions
- [ ] Shows error type, message, stack trace in code block
- [ ] Scrollable if content is long

### Toast notification system

- [ ] `useToast()` hook: `{ success(msg), error(msg), info(msg) }`
- [ ] Success: green background, ✓ icon
- [ ] Error: red background, ✗ icon
- [ ] Info: blue background, ℹ icon
- [ ] Auto-dismiss after 5 seconds
- [ ] Manual dismiss via X button
- [ ] Slide-in animation (right → left)
- [ ] Multiple toasts stack vertically
- [ ] Fixed position: bottom-right corner

### API client

- [ ] `request<T>()` base function with JWT injection
- [ ] `BASE_URL` from `NEXT_PUBLIC_API_URL` env, fallback `http://localhost:8080`
- [ ] Content-Type auto-set to `application/json` (except FormData)
- [ ] Response unwrapped: checks `success`, returns `data`, throws on failure
- [ ] Namespaced: `api.auth.*`, `api.collection.*`, `api.analysis.*`, `api.triage.*`, `api.reporting.*`

---

## Summary

| # | Feature Area | Priority | Backend | Frontend | Total |
|---|-------------|----------|---------|----------|-------|
| 1 | User Management | P1 | 14 | 18 | 32 |
| 2 | Project Management | P1 | 12 | 0 | 12 |
| 3 | Test Run | P0 | 21 | 0 | 21 |
| 4 | Test Execution | P0 | 9 | 8 | 17 |
| 5 | Analysis Engine | P0 | 30 | 6 | 36 |
| 6 | Failure Triage | P1 | 12 | 13 | 25 |
| 7 | Reporting | P2 | 11 | 0 | 11 |
| 8 | Frontend Shared | P2 | 0 | 28 | 28 |
| **Total** | | | **109** | **73** | **182** |

Verification order follows the dependency chain: you can't verify later sections until earlier ones work.

---

## Implementation Gaps Found During Audit

These are requirements that are specified but not implemented, or bugs that cause incorrect behavior. Fixes needed before verification can pass.

### Bugs (behavior contradicts requirements)

| # | Area | Requirement | Actual Behavior | Severity |
|---|------|-------------|-----------------|----------|
| 1 | User Mgmt | Register with missing email → 400 | Returns 500 "Internal server error" (fixed) | High |
| 2 | User Mgmt | Register with duplicate email → proper error | `DataIntegrityViolationException` → 500, no `existsByEmail` check | High |
| 3 | Test Run | `runIdentifier` is optional param | DB column is `nullable = false`, causes SQL error → 500 if omitted | High |
| 4 | Test Exec | Retry detection from XML | `attempt` hardcoded to 1, retries never detected | High |
| 5 | Reporting | `overallPassRate` in summary | Hardcoded to 0, never computed | High |
| 6 | Reporting | `totalRuns` in summary | Hardcoded to 0, never computed | High |
| 7 | Reporting | `topFlakyTests` in summary | Hardcoded to empty list, never queried | High |
| 8 | Analysis | `retryRate` in TrendSnapshot | Hardcoded to 0.0, never computed | Medium |
| 9 | Analysis | `FailureCluster.clusterKey` unique globally | Not scoped to projectId — cross-project collisions cause 500 | Medium |
| 10 | User Mgmt | `GET /auth/me` with deleted user | `orElseThrow()` with no arg → `NoSuchElementException` → 500 | Medium |
| 11 | Frontend | Settings page fetches project | Uses raw `fetch()` with hardcoded localhost instead of `api` client | Medium |
| 12 | Frontend | Settings page team members | Hardcoded "Alice ADMIN, Bob QA, Carol VIEWER" instead of API call | Medium |
| 13 | Frontend | Dashboard project name | Hardcoded to "Project" instead of fetching from API | Low |
| 14 | Frontend | Login redirect | Hardcoded to `/projects/1`, wrong for users not in project 1 | Low |

### Missing Features (specified but not implemented)

| # | Area | Missing |
|---|------|---------|
| 1 | User Mgmt | No `existsByEmail` check before registration |
| 2 | User Mgmt | No 401 handling in frontend API client (expired token → cryptic error) |
| 3 | User Mgmt | No route protection middleware (unauthenticated users can access any page) |
| 4 | Project Mgmt | No REST endpoints for TenantService CRUD (createOrg, createTeam, etc.) |
| 5 | Test Run | No `<testsuites>` wrapper support in XML parser |
| 6 | Test Run | No duplicate upload detection (re-upload → 500 from unique constraint) |
| 7 | Test Exec | No `@Min(1)` validation on attempt field |
| 8 | Analysis | Environments hardcoded to `{"dev", "staging", "prod"}` — custom envs ignored |
| 9 | Analysis | No scheduled backfill for trend snapshots on days without uploads |
| 10 | Analysis | No per-project analysis config overrides |
| 11 | Triage | No project membership validation (any authenticated user can triage any execution) |
| 12 | Triage | Summary 30-day window hardcoded, no parameter |
| 13 | Frontend | Sidebar has no link to Test Runs list / Test Explorer |
| 14 | Frontend | N+1 triage queries in TestTable (one request per execution) |
| 15 | Frontend | No loading skeleton states (just text spinners) |
| 16 | Frontend | PassRateChart Y-axis hardcoded to [80, 100] — sub-80% pass rates invisible |
| 17 | Frontend | No empty state message when FilterBar matches zero executions |
| 18 | Frontend | `envFilter` in FilterBar is set but never applied to filtered executions |
| 19 | All | No pagination metadata in API responses (page number, total pages, total count) |
