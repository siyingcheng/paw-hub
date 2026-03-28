# Story 5: Project Management (CRUD Operations)

**Sprint**: 3  
**Priority**: P1 (High)  
**Story Points**: 8  
**Status**: Not Started

## Summary

Implement CRUD operations for project management, allowing users to create, read, update, and delete projects with proper access control.

## Description

As a QA manager, I want to manage projects so that I can organize test suites and control access to testing data within my organization.

## Acceptance Criteria

### 1. Create Project
- [ ] POST `/api/v1/projects` endpoint implemented
- [ ] Only authenticated users can create projects
- [ ] Project name must be unique within organization
- [ ] Project name validation (3-100 characters, alphanumeric + spaces)
- [ ] Description field optional (max 500 characters)
- [ ] Creator automatically set as owner
- [ ] Project initialized with active status
- [ ] Returns created project object with ID

### 2. Read Projects
- [ ] GET `/api/v1/projects` endpoint lists all accessible projects
- [ ] GET `/api/v1/projects/{id}` returns specific project details
- [ ] Pagination implemented (limit, offset parameters)
- [ ] Filtering by status (active/inactive)
- [ ] Sorting by name, creation date, or update date
- [ ] Only projects user has access to are returned
- [ ] Include owner and member count in list view

### 3. Update Project
- [ ] PUT `/api/v1/projects/{id}` endpoint implemented
- [ ] Only owner or admin can update project
- [ ] Update name, description, and status
- [ ] Name uniqueness validation on update
- [ ] Version control or timestamps show last update
- [ ] Returns updated project object
- [ ] Audit log entry created

### 4. Delete Project
- [ ] DELETE `/api/v1/projects/{id}` endpoint implemented
- [ ] Only owner or admin can delete project
- [ ] Soft delete (mark as deleted, retain data)
- [ ] All related test suites and executions preserved or cascaded
- [ ] Returns success confirmation
- [ ] Audit log entry created with deletion reason

### 5. Project Access Control
- [ ] Owner has full control (read, write, delete, admin)
- [ ] Members can read project if granted access
- [ ] Members can create test suites only if granted permission
- [ ] Access list manageable (add/remove members)
- [ ] Role-based permissions (admin, editor, viewer)
- [ ] Default role for new members configurable

### 6. Frontend Project Management
- [ ] Projects list page at `/projects`
- [ ] Project cards showing name, description, member count
- [ ] Create project button and modal form
- [ ] Edit project modal with all fields
- [ ] Delete confirmation dialog
- [ ] Link to project details page
- [ ] Quick access to manage members and suites

### 7. Project Details Page
- [ ] URL: `/projects/{id}`
- [ ] Display project information
- [ ] List of test suites in project
- [ ] Team members and their roles
- [ ] Project activity/recent executions
- [ ] Settings button to edit project
- [ ] Add members button

## Technical Details

### API Endpoints

```
GET /api/v1/projects
  - Query params: limit, offset, status, sort_by
  - Returns: Array of projects with owner info

POST /api/v1/projects
  - Body: { name, description }
  - Returns: Created project object

GET /api/v1/projects/{id}
  - Returns: Project details with members

PUT /api/v1/projects/{id}
  - Body: { name, description, status }
  - Returns: Updated project object

DELETE /api/v1/projects/{id}
  - Returns: { success: true, message }
```

### Project Object Schema
```json
{
  "id": "uuid",
  "name": "Mobile App Testing",
  "description": "Test suite for mobile application",
  "owner_id": "uuid",
  "owner": {
    "id": "uuid",
    "email": "owner@example.com",
    "full_name": "John Doe"
  },
  "members": [
    {
      "id": "uuid",
      "email": "user@example.com",
      "role": "editor"
    }
  ],
  "is_active": true,
  "member_count": 5,
  "suite_count": 12,
  "created_at": "2026-03-28T10:00:00Z",
  "updated_at": "2026-03-28T10:00:00Z"
}
```

### Access Control Matrix

| Role | Create | Read | Update | Delete | Manage Members |
|------|--------|------|--------|--------|-----------------|
| Owner | ✓ | ✓ | ✓ | ✓ | ✓ |
| Admin | ✓ | ✓ | ✓ | ✓ | ✓ |
| Editor | ✗ | ✓ | ✓ | ✗ | ✗ |
| Viewer | ✗ | ✓ | ✗ | ✗ | ✗ |

## Dependencies
- Story 1: Project Initialization
- Story 2: Database Schema Setup
- Story 4: User Login Authentication

## Related Stories
- Story 6: Test Suite Management
- Story 8: Project Member Management (future)

## Definition of Done
- [ ] All CRUD endpoints implemented and tested
- [ ] Frontend pages created with proper styling
- [ ] Access control properly enforced
- [ ] Unit tests written (>90% coverage)
- [ ] Integration tests for all endpoints
- [ ] Audit logging working
- [ ] Error handling and validation complete
- [ ] API documentation updated

## Testing Checklist
- [ ] Create project with valid data succeeds
- [ ] Create project with duplicate name fails
- [ ] Only authenticated users can create
- [ ] Read operations return only accessible projects
- [ ] Update only by owner/admin succeeds
- [ ] Delete creates soft delete record
- [ ] Pagination works correctly
- [ ] Filtering and sorting work
- [ ] Access control properly enforced

## Notes
- Consider adding project cloning capability
- May add project templates in future
- Consider project-level settings/configuration
- Archive instead of delete option helpful for UI
