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

---

## Gherkin Scenarios

### Project Info

```gherkin
Feature: Project Information

  Scenario: Get project details
    Given project 1 belongs to team "QA Team" in org "PawCorp" with API key "sk-proj-abc123..."
    When I GET /api/v1/projects/1 as an authenticated user
    Then the response status is 200
    And response contains name, apiKey, teamName, and orgName

  Scenario: Get non-existent project
    Given project 999 does not exist
    When I GET /api/v1/projects/999 as an authenticated user
    Then the response status is 404

  Scenario: API key format is valid
    Given a new project is created
    Then the apiKey starts with "sk-proj-"
    And the apiKey is 28 characters long
```

### Role Query

```gherkin
Feature: Role Query

  Scenario: User is ADMIN in project's team
    Given user "alice" has role ADMIN in team "QA Team"
    And project 1 belongs to team "QA Team"
    When I GET /api/v1/projects/1/my-role as "alice"
    Then the response status is 200
    And the role is "ADMIN"

  Scenario: User is QA in project's team
    Given user "bob" has role QA in team "QA Team"
    And project 1 belongs to team "QA Team"
    When I GET /api/v1/projects/1/my-role as "bob"
    Then the response status is 200
    And the role is "QA"

  Scenario: User is not a member of project's team
    Given user "stranger" has no membership in team "QA Team"
    When I GET /api/v1/projects/1/my-role as "stranger"
    Then the response status is 200
    And the role is "NONE"
```

### Tenant Service (Backend)

```gherkin
Feature: Tenant Management

  Scenario: Create organization
    When TenantService.createOrg("AcmeCorp") is called
    Then an Organization named "AcmeCorp" is persisted

  Scenario: Create team under organization
    Given an organization "AcmeCorp" exists with id 2
    When TenantService.createTeam(2, "Engineering") is called
    Then a Team named "Engineering" is persisted and linked to org 2

  Scenario: Create project with API key
    Given a team "Engineering" exists with id 2
    And user "alice" is a member of team 2
    When TenantService.createProject(2, "web-app", aliceUserId) is called
    Then a Project named "web-app" is persisted with an auto-generated apiKey

  Scenario: Create project fails for non-member
    Given a team "Engineering" exists with id 2
    And user "bob" is NOT a member of team 2
    When TenantService.createProject(2, "web-app", bobUserId) is called
    Then a PawHubException is thrown with status 403

  Scenario: Add member to team
    Given a team "Engineering" exists with id 2
    And user "carol" is not a member of team 2
    When TenantService.addMember(2, carolUserId, QA) is called
    Then a Membership is created with role QA

  Scenario: Add duplicate member fails
    Given user "alice" is already a member of team 2
    When TenantService.addMember(2, aliceUserId, QA) is called
    Then a PawHubException is thrown with status 409
```

## Out of Scope (v1)

- Tenant CRUD UI (no create/edit org/team/project screens)
- API key rotation
- Project-level analysis config overrides
