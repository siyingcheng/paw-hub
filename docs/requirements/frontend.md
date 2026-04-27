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
