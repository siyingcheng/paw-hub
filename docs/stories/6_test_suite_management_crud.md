# Story 6: Test Suite Management (CRUD Operations)

**Sprint**: 3  
**Priority**: P1 (High)  
**Story Points**: 8  
**Status**: Not Started

## Summary

Implement CRUD operations for test suite management, enabling users to organize and manage test suites within projects.

## Description

As a QA engineer, I want to manage test suites within a project so that I can organize and categorize automated tests for better tracking and analysis.

## Acceptance Criteria

### 1. Create Test Suite
- [ ] POST `/api/v1/projects/{projectId}/suites` endpoint implemented
- [ ] Only project members with edit permission can create
- [ ] Suite name must be unique within project
- [ ] Suite name validation (3-100 characters)
- [ ] Description optional (max 500 characters)
- [ ] Tags support for categorization (optional)
- [ ] Default owner is creator
- [ ] Suite initialized with active status
- [ ] Returns created suite object

### 2. Read Test Suites
- [ ] GET `/api/v1/projects/{projectId}/suites` lists all suites in project
- [ ] GET `/api/v1/suites/{id}` returns suite details
- [ ] Pagination support (limit, offset)
- [ ] Filtering by status and tags
- [ ] Sorting by name, creation date, or test count
- [ ] Include test count and latest execution info
- [ ] Return only suites accessible to user's projects

### 3. Update Test Suite
- [ ] PUT `/api/v1/suites/{id}` endpoint implemented
- [ ] Only owner or project lead can update
- [ ] Update name, description, tags, and status
- [ ] Name uniqueness validation within project
- [ ] Audit log entry created
- [ ] Returns updated suite object

### 4. Delete Test Suite
- [ ] DELETE `/api/v1/suites/{id}` endpoint implemented
- [ ] Only owner or admin can delete
- [ ] Soft delete (mark as deleted, retain test history)
- [ ] Option to delete related test executions
- [ ] Returns success confirmation
- [ ] Audit log entry created

### 5. Suite Metadata & Information
- [ ] Track total test count in suite
- [ ] Track latest execution date and status
- [ ] Calculate average pass rate
- [ ] Calculate average execution duration
- [ ] Store owner information
- [ ] Support tags for categorization (e.g., "regression", "smoke")

### 6. Frontend Test Suite Management
- [ ] List page showing all suites in project
- [ ] Suite cards with key metrics
- [ ] Create suite modal/form
- [ ] Edit suite modal
- [ ] Delete confirmation dialog
- [ ] Filter by tags and status
- [ ] Sort options
- [ ] Quick actions (view, edit, delete)

### 7. Suite Details Page
- [ ] URL: `/projects/{projectId}/suites/{id}`
- [ ] Display suite information and metrics
- [ ] List of recent test executions
- [ ] Test count and pass rate trends
- [ ] Team members with edit access
- [ ] Edit and delete buttons

## Technical Details

### API Endpoints

```
GET /api/v1/projects/{projectId}/suites
  - Query: limit, offset, status, tags, sort_by
  - Returns: Array of test suites

POST /api/v1/projects/{projectId}/suites
  - Body: { name, description, tags }
  - Returns: Created suite object

GET /api/v1/suites/{id}
  - Returns: Suite details with metrics

PUT /api/v1/suites/{id}
  - Body: { name, description, tags, status }
  - Returns: Updated suite object

DELETE /api/v1/suites/{id}
  - Query: cascade (optional, delete related data)
  - Returns: { success: true, message }
```

### Test Suite Object Schema
```json
{
  "id": "uuid",
  "project_id": "uuid",
  "name": "Regression Tests - Web",
  "description": "Full regression test suite for web platform",
  "tags": ["regression", "web", "critical"],
  "owner_id": "uuid",
  "owner": {
    "id": "uuid",
    "email": "owner@example.com",
    "full_name": "Jane Smith"
  },
  "is_active": true,
  "test_count": 145,
  "latest_execution": {
    "id": "uuid",
    "status": "passed",
    "started_at": "2026-03-28T09:00:00Z",
    "total_tests": 145,
    "passed_count": 143,
    "failed_count": 2,
    "pass_rate": 98.62
  },
  "average_pass_rate": 96.5,
  "average_duration_seconds": 450,
  "created_at": "2026-03-28T10:00:00Z",
  "updated_at": "2026-03-28T10:00:00Z"
}
```

### Suite Status Enumeration
```
- active: Suite is actively used
- archived: Suite is no longer used but data retained
- deleted: Soft deleted (not shown in normal queries)
```

### Tags System
- Predefined tags (optional): "regression", "smoke", "sanity", "e2e", "unit", "integration", "critical"
- Custom tags allowed
- Tags used for filtering and organization
- Tags queryable via search

## Dependencies
- Story 1: Project Initialization
- Story 2: Database Schema Setup
- Story 4: User Login Authentication
- Story 5: Project Management

## Related Stories
- Story 7: Test Result Ingestion API
- Story 8: Test Result Details View

## Definition of Done
- [ ] All CRUD endpoints implemented and tested
- [ ] Frontend pages created with proper UI/UX
- [ ] Access control properly enforced
- [ ] Metrics calculation accurate
- [ ] Unit tests written (>90% coverage)
- [ ] Integration tests for all endpoints
- [ ] Audit logging working
- [ ] Error handling and validation complete
- [ ] API documentation updated

## Testing Checklist
- [ ] Create suite with valid data succeeds
- [ ] Create suite with duplicate name fails within project
- [ ] Only authorized users can create/edit
- [ ] Read operations return only accessible suites
- [ ] Update only by owner/admin succeeds
- [ ] Delete creates soft delete record
- [ ] Metrics calculated correctly
- [ ] Tags properly indexed and searchable
- [ ] Pagination and filtering work
- [ ] Sorting works for all fields

## Implementation Notes
- Use database views for metric calculations
- Cache metrics with 1-hour TTL
- Consider materialized views for trending metrics
- Implement denormalization for frequently accessed metrics

## Notes
- Tags can be expanded with ML-based categorization in future
- Consider suite templates for common patterns
- May add suite-level webhooks in future
- Consider suite execution scheduling in future
