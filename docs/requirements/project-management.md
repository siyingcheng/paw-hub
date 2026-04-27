# Project Management (Multi-Tenancy)

**Priority:** P1 | **Version:** v1

## Overview

Three-level hierarchy: Organization → Team → Project. Each project has an API key for test result upload. Users belong to teams with specific roles. Backend service layer exists; CRUD API endpoints not yet exposed in v1.

## Data Model

```
Organization
  id: Long (PK)
  name: String
  createdAt: Instant

Team
  id: Long (PK)
  name: String
  org_id: Long (FK → Organization)

Project
  id: Long (PK)
  name: String
  team_id: Long (FK → Team)
  apiKey: String (unique, auto-generated "sk-proj-...")
  createdAt: Instant
```

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/v1/projects/{id}` | Yes | Project details (name, apiKey, team, org) |
| GET | `/api/v1/projects/{id}/my-role` | Yes | Current user's role in this project |

## Acceptance Criteria

### Project Info

- [ ] `GET /projects/{id}` returns project name, apiKey, team name, org name
- [ ] Non-existent project → 404
- [ ] API key format: `sk-proj-` + 20 random chars

### Role Query

- [ ] `GET /projects/{id}/my-role` returns role for current user in project's team
- [ ] User not in team → returns "NONE"
- [ ] User is ADMIN → returns "ADMIN"
- [ ] User is QA → returns "QA"

### Tenant Service (Backend, UI not in v1)

- [ ] `createOrg(name)` → Organization persisted
- [ ] `createTeam(orgId, name)` → Team persisted, linked to Org
- [ ] `createProject(teamId, name, userId)` → Project persisted, auto-generates apiKey
- [ ] `createProject` fails if user is not team member → 403
- [ ] `addMember(teamId, userId, role)` → Membership created
- [ ] `addMember` fails if already a member → 409

## Out of Scope (v1)

- Tenant CRUD UI (no create/edit org/team/project screens)
- API key rotation
- Project-level analysis config overrides
