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

### Role-Based Access (Frontend)

- [ ] ADMIN sees Sidebar with: Dashboard, Trends, Settings
- [ ] QA sees Sidebar with: Dashboard, Trends (no Settings)
- [ ] VIEWER sees Sidebar with: Dashboard, Trends (no Settings)
- [ ] Settings page redirects non-ADMIN to dashboard with error toast
- [ ] Role badge shown in sidebar footer (ADMIN/QA/VIEWER)
- [ ] User registers → has no team membership (must be added by admin)
