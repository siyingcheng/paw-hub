# Frontend

**Priority:** P2 | **Version:** v1

## Overview

Next.js 15 (App Router) + TypeScript + Tailwind CSS + Recharts. Five pages: Login, Register, Dashboard, Test Explorer, Trends, Settings.

## Tech Stack

| Layer | Choice |
|-------|--------|
| Framework | Next.js 15 (App Router) |
| Language | TypeScript (strict) |
| Styling | Tailwind CSS v4 |
| Charts | Recharts 2.x |
| Auth | JWT stored in localStorage, sent via Authorization header |

---

## Page: Login (`/login`)

Static page. Username + password form. Redirects to dashboard on success.

- [ ] Username and password inputs
- [ ] Submit calls `POST /auth/login`
- [ ] Success: JWT stored, redirect to `/projects/1`
- [ ] Success: green toast "Welcome, {username}"
- [ ] Failure: red toast with error message
- [ ] Button shows "Logging in..." while loading, disabled
- [ ] Link to Register page

## Page: Register (`/register`)

Static page. Username + email + password form. Redirects to login on success.

- [ ] Username, email, password inputs
- [ ] Submit calls `POST /auth/register`
- [ ] Success: green toast, redirect to `/login`
- [ ] Failure: red toast with error message
- [ ] Button shows "Registering..." while loading, disabled
- [ ] Link to Login page

## Page: Dashboard (`/projects/{id}`)

Server-rendered. KPI cards, pass rate trend chart, regressions, triage breakdown.

- [ ] KPI cards: Latest Pass Rate, Flaky Tests count, Untriaged count, Total Triaged
- [ ] PassRateChart: Recharts LineChart with dev/staging/prod lines
- [ ] RecentRegressions: run-level alerts + case-level regressed tests
- [ ] TriageBreakdown: 8-category grid with counts
- [ ] Loading spinner while data loads
- [ ] Error toast on API failure
- [ ] Sidebar with role-based links
- [ ] Navbar with project name + logout button

## Page: Test Explorer (`/projects/{id}/runs/{runId}`)

Server-rendered. Run summary bar, filterable test table, inline triage.

- [ ] Run summary: passed/failed/skipped counts, duration, environment, branch
- [ ] FilterBar: search input + status dropdown
- [ ] TestTable: all executions with color-coded status badges
- [ ] Triage button on failed executions → TriageModal
- [ ] Error detail expandable (message + stack trace)
- [ ] Loading spinner while data loads
- [ ] Error toast on API failure

## Page: Trends (`/projects/{id}/trends`)

Server-rendered. Trend charts, flaky tests table, failure clusters table.

- [ ] PassRateChart (shared component with Dashboard)
- [ ] Flaky tests table: test case key, score, transition count
- [ ] Failure clusters table: representative error, occurrence count
- [ ] Loading spinner while data loads
- [ ] Empty states when no data

## Page: Settings (`/projects/{id}/settings`)

Server-rendered. ADMIN only. Project config, API key, team members.

- [ ] Non-ADMIN redirected to dashboard with error toast
- [ ] Project info: name, org, team, API key (with code styling)
- [ ] Upload endpoint shown: `POST /api/v1/projects/{id}/test-results`
- [ ] Analysis thresholds: flaky, regression sigma, window
- [ ] Team members list with role badges

---

## Components

### Sidebar

- [ ] Navigation links: Dashboard, Trends (all roles), Settings (ADMIN only)
- [ ] Active link highlighted (blue background)
- [ ] Role badge in footer

### Navbar

- [ ] "Paw-Hub" branding + project name
- [ ] Logout button (clears JWT, redirects to /login)

### FilterBar

- [ ] Search input (filters by test name and class name, case-insensitive)
- [ ] Status dropdown (All, PASS, FAIL, SKIP, ERROR)

### TriageModal

- [ ] Category dropdown (8 options)
- [ ] Issue link input
- [ ] Comment textarea
- [ ] Save + Cancel buttons
- [ ] Pre-fills existing triage data
- [ ] Shows loading state on save

### ErrorDetail

- [ ] Expandable "Details" button on failed executions
- [ ] Shows error type, message, stack trace in code block
- [ ] Scrollable if content is long

### PassRateChart

- [ ] Recharts LineChart
- [ ] 3 lines: dev (blue), staging (yellow), prod (green)
- [ ] Tooltip, legend, grid
- [ ] Y-axis auto-scaled (not hardcoded)
- [ ] Empty state when no data

---

## Toast Notification System

- [ ] `useToast()` hook: `{ success(msg), error(msg), info(msg) }`
- [ ] Success: green background, ✓ icon
- [ ] Error: red background, ✗ icon
- [ ] Info: blue background, ℹ icon
- [ ] Auto-dismiss after 5 seconds
- [ ] Manual dismiss via X button
- [ ] Slide-in animation (right → left)
- [ ] Multiple toasts stack vertically
- [ ] Fixed position: bottom-right corner

---

## API Client

- [ ] `request<T>()` base function with JWT injection
- [ ] `BASE_URL` from `NEXT_PUBLIC_API_URL` env, fallback `http://localhost:8080`
- [ ] Content-Type auto-set to `application/json` (except FormData)
- [ ] Response unwrapped: checks `success`, returns `data`, throws on failure
- [ ] Namespaced: `api.auth.*`, `api.collection.*`, `api.analysis.*`, `api.triage.*`, `api.reporting.*`

---

## Gherkin Scenarios

### Login Page

```gherkin
Feature: Login Page

  Scenario: Successful login
    Given I am on /login
    When I enter username "alice" and password "pass123"
    And I click the submit button
    Then the button shows "Logging in..." and is disabled
    And on success, JWT is stored in localStorage
    And I am redirected to /projects/1
    And a green toast "Welcome, alice" appears

  Scenario: Failed login
    Given I am on /login
    When I enter wrong credentials and submit
    Then a red toast appears with the error message
    And I remain on /login

  Scenario: Navigation to register page
    Given I am on /login
    When I click the register link
    Then I am taken to /register
```

### Register Page

```gherkin
Feature: Register Page

  Scenario: Successful registration
    Given I am on /register
    When I enter username "newuser", email "new@example.com", password "secret123"
    And I click the submit button
    Then the button shows "Registering..." and is disabled
    And on success, a green toast appears
    And I am redirected to /login

  Scenario: Failed registration
    Given I am on /register with an existing username
    When I submit the form
    Then a red toast appears with the error message
```

### Dashboard Page

```gherkin
Feature: Dashboard Page

  Scenario: View dashboard with data
    Given I am authenticated and on /projects/1
    When the page loads
    Then I see KPI cards showing Latest Pass Rate, Flaky Tests count, Untriaged count, and Total Triaged
    And I see a PassRateChart with 3 lines (dev, staging, prod)
    And I see RecentRegressions with run-level and case-level alerts
    And I see a TriageBreakdown widget with 8 categories
    And the sidebar shows role-based links
    And the navbar shows the project name and logout button

  Scenario: Dashboard loading state
    Given I am on /projects/1
    When the page is fetching data
    Then I see a loading spinner

  Scenario: Dashboard API error
    Given the API is unreachable
    When the dashboard loads
    Then an error toast appears

  Scenario: KPI cards show placeholder when no data
    Given project 1 has no trend data
    When I view the dashboard
    Then KPI cards show "—" for missing values
```

### Trends Page

```gherkin
Feature: Trends Page

  Scenario: View trends with data
    Given I am on /projects/1/trends
    When the page loads
    Then I see a PassRateChart
    And I see a flaky tests table with test case key, score, and transition count
    And I see a failure clusters table with representative error and occurrence count

  Scenario: Empty states for no data
    Given project 1 has no flaky tests and no failure clusters
    When I view /projects/1/trends
    Then empty state messages are shown for each empty section
```

### Settings Page

```gherkin
Feature: Settings Page

  Scenario: ADMIN views settings
    Given I am logged in as an ADMIN
    When I navigate to /projects/1/settings
    Then I see project info: name, org, team
    And I see the API key displayed in code styling
    And I see the upload endpoint: POST /api/v1/projects/1/test-results
    And I see analysis threshold values
    And I see the team members list with role badges

  Scenario: Non-ADMIN redirected from settings
    Given I am logged in as QA
    When I navigate to /projects/1/settings
    Then I am redirected to /projects/1
    And an error toast is shown
```

### Navbar

```gherkin
Feature: Navbar

  Scenario: Navbar displays branding and project name
    Given I am on a project page
    Then the navbar shows "Paw-Hub" branding
    And the navbar shows the current project name

  Scenario: Logout clears auth and redirects
    Given I am logged in
    When I click the logout button
    Then the JWT is cleared from localStorage
    And I am redirected to /login
```

### PassRateChart

```gherkin
Feature: Pass Rate Chart

  Scenario: Chart renders with data
    Given trend data exists for dev, staging, and prod
    When PassRateChart renders
    Then 3 lines are displayed: dev (blue), staging (yellow), prod (green)
    And tooltip, legend, and grid are shown
    And the Y-axis is auto-scaled

  Scenario: Chart with empty data
    Given no trend data exists
    When PassRateChart renders
    Then an empty state is shown
```

### Toast System

```gherkin
Feature: Toast Notifications

  Scenario: Show success toast
    When useToast().success("Operation completed")
    Then a green toast appears with ✓ icon
    And the message is "Operation completed"
    And it auto-dismisses after 5 seconds

  Scenario: Show error toast
    When useToast().error("Something went wrong")
    Then a red toast appears with ✗ icon

  Scenario: Show info toast
    When useToast().info("Loading data...")
    Then a blue toast appears with ℹ icon

  Scenario: Manual dismiss
    When a toast is visible
    And I click the X button
    Then the toast is dismissed immediately

  Scenario: Multiple toasts stack
    When 3 toasts are triggered rapidly
    Then all 3 are visible stacked vertically in the bottom-right corner
```

### API Client

```gherkin
Feature: API Client

  Scenario: Request includes JWT in Authorization header
    Given I have a JWT stored in localStorage
    When any api.* method is called
    Then the request includes "Authorization: Bearer <token>"

  Scenario: Request without JWT
    Given no JWT is stored
    When any api.* method is called
    Then Authorization header is omitted

  Scenario: BASE URL from environment variable
    Given NEXT_PUBLIC_API_URL is set to "https://api.example.com"
    When any api.* method is called
    Then the request URL starts with "https://api.example.com"

  Scenario: BASE URL fallback
    Given NEXT_PUBLIC_API_URL is not set
    When any api.* method is called
    Then the request URL starts with "http://localhost:8080"

  Scenario: Content-Type for JSON requests
    When api.auth.login("alice", "pass123") is called
    Then the Content-Type header is "application/json"

  Scenario: Content-Type omitted for FormData
    When api.collection.uploadXml is called with a FormData body
    Then Content-Type is NOT set to "application/json"

  Scenario: Successful response unwrapped
    Given the API returns {"success": true, "data": {"token": "abc"}}
    When api.auth.login resolves
    Then the returned value is {"token": "abc"}

  Scenario: Failed response throws error
    Given the API returns {"success": false, "message": "Invalid credentials"}
    When api.auth.login is called
    Then an Error is thrown with message "Invalid credentials"
```
