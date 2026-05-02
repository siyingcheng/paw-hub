# User Management

**Priority:** P1 | **Version:** v1

## Overview

JWT-based authentication with role-based access control. Users register accounts, log in to obtain tokens, and are assigned roles within teams.

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/auth/register` | No | Create account |
| POST | `/api/v1/auth/login` | No | Login, get JWT |
| GET | `/api/v1/auth/me` | Yes | Current user + memberships |

## Data Model

```
User
  id: Long (PK)
  username: String (unique)
  email: String (unique)
  passwordHash: String
  createdAt: Instant

Membership
  id: Long (PK)
  user_id: Long (FK → User)
  team_id: Long (FK → Team)
  role: enum (ADMIN | QA | VIEWER)
  unique: (user_id, team_id)
```

## Roles

| Role | Permissions |
|------|-------------|
| ADMIN | Full access: dashboard, trends, settings, manage team members, view API key |
| QA | Dashboard, trends, test explorer, triage failed cases |
| VIEWER | Dashboard, trends (read-only) |

## Acceptance Criteria

### Registration

- [x] Register with unique username and valid email → 200, token returned `{ token, userId, username }`
- [x] Register with duplicate username → 409, "Username already taken"
- [x] Register with missing/invalid email → 400
- [x] Password is BCrypt-hashed before storage (never plaintext)

### Login

- [x] Login with correct credentials → 200, JWT token returned
- [x] Login with wrong password → 401, "Invalid credentials"
- [x] Login with non-existent username → 401, "Invalid credentials"
- [x] Token contains: userId (sub), username (claim), issuedAt, expiration
- [x] Token expires after configured duration (default: 24h)

### Auth Filter

- [x] Requests without Authorization header → continue without auth (handled by security config)
- [x] Requests with `Bearer <valid_token>` → userId set in SecurityContext
- [x] Requests with `Bearer <invalid_token>` → continue without auth (no crash)
- [ ] Requests with `Bearer <expired_token>` → continue without auth

### Current User

- [x] `GET /auth/me` returns user id, username, email, and all memberships with roles
- [x] Unauthenticated request → 401/403

### Role-Based Access (Frontend)

- [x] ADMIN sees Sidebar with: Dashboard, Trends, Settings
- [x] QA sees Sidebar with: Dashboard, Trends (no Settings)
- [x] VIEWER sees Sidebar with: Dashboard, Trends (no Settings)
- [x] Settings page redirects non-ADMIN to dashboard with error toast
- [x] Role badge shown in sidebar footer (ADMIN/QA/VIEWER)
- [x] User registers → has no team membership (must be added by admin)

---

## Gherkin Scenarios

### Registration

```gherkin
Feature: User Registration

  Scenario: Register with valid data
    Given no user exists with username "alice"
    When I POST /api/v1/auth/register with {"username": "alice", "email": "alice@example.com", "password": "secret123"}
    Then the response status is 200
    And the response contains "token", "userId", and "username"

  Scenario: Register with duplicate username
    Given a user "alice" already exists
    When I POST /api/v1/auth/register with {"username": "alice", "email": "another@example.com", "password": "secret123"}
    Then the response status is 409
    And the response message is "Username already taken"

  Scenario: Register with missing email
    When I POST /api/v1/auth/register with {"username": "bob", "password": "secret123"}
    Then the response status is 400
    And the response message contains "email"

  Scenario: Register with invalid email format
    When I POST /api/v1/auth/register with {"username": "bob", "email": "not-an-email", "password": "secret123"}
    Then the response status is 400
    And the response message contains "email"

  Scenario: Register with blank username
    When I POST /api/v1/auth/register with {"username": "", "email": "bob@example.com", "password": "secret123"}
    Then the response status is 400
    And the response message contains "username"

  Scenario: Password is stored as BCrypt hash
    Given I register with {"username": "alice", "email": "alice@example.com", "password": "secret123"}
    When I query the database for user "alice"
    Then the password_hash column does NOT contain "secret123"
    And password_hash starts with "$2a$"
```

### Login

```gherkin
Feature: User Login

  Scenario: Login with correct credentials
    Given a user "alice" exists with password "secret123"
    When I POST /api/v1/auth/login with {"username": "alice", "password": "secret123"}
    Then the response status is 200
    And the response contains a valid JWT token
    And the token payload contains "userId" as subject and "username" claim

  Scenario: Login with wrong password
    Given a user "alice" exists with password "secret123"
    When I POST /api/v1/auth/login with {"username": "alice", "password": "wrong"}
    Then the response status is 401
    And the response message is "Invalid credentials"

  Scenario: Login with non-existent username
    Given no user "ghost" exists
    When I POST /api/v1/auth/login with {"username": "ghost", "password": "whatever"}
    Then the response status is 401
    And the response message is "Invalid credentials"

  Scenario: Login with blank fields
    When I POST /api/v1/auth/login with {"username": "", "password": ""}
    Then the response status is 400

  Scenario: Token expires after configured duration
    Given I log in and receive a token
    When 24 hours and 1 minute have passed
    Then requests with that token are treated as unauthenticated
```

### Current User

```gherkin
Feature: Current User Info

  Scenario: Get current user with memberships
    Given I am authenticated as "alice" who belongs to "QA Team" as ADMIN
    When I GET /api/v1/auth/me
    Then the response status is 200
    And response contains id, username, email
    And response contains memberships array
    And one membership has role "ADMIN" and teamName "QA Team"

  Scenario: Get current user without authentication
    When I GET /api/v1/auth/me without an Authorization header
    Then the response status is 401 or 403
```

### Role-Based Access (Frontend)

```gherkin
Feature: Role-Based Sidebar Navigation

  Scenario: ADMIN sees all navigation links
    Given I am logged in as a user with role "ADMIN" in project 1
    When I view the sidebar on /projects/1
    Then I see "Dashboard", "Trends", and "Settings" links
    And I see an "ADMIN" role badge

  Scenario: QA does not see Settings link
    Given I am logged in as a user with role "QA" in project 1
    When I view the sidebar on /projects/1
    Then I see "Dashboard" and "Trends" links
    And I do NOT see "Settings" link
    And I see a "QA" role badge

  Scenario: VIEWER does not see Settings link
    Given I am logged in as a user with role "VIEWER" in project 1
    When I view the sidebar on /projects/1
    Then I see "Dashboard" and "Trends" links
    And I do NOT see "Settings" link
    And I see a "VIEWER" role badge

  Scenario: Non-ADMIN redirected from Settings page
    Given I am logged in as a user with role "QA" in project 1
    When I navigate to /projects/1/settings
    Then I am redirected to /projects/1
    And I see an error toast

  Scenario: Newly registered user has no memberships
    Given I register a new user "newuser"
    When I GET /api/v1/auth/me
    Then the memberships array is empty
```
