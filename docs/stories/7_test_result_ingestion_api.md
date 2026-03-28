# Story 7: Test Result Ingestion API

**Sprint**: 4  
**Priority**: P0 (Critical)  
**Story Points**: 13  
**Status**: Not Started

## Summary

Implement a robust API for ingesting and processing test execution results from various CI/CD platforms and testing frameworks.

## Description

As a CI/CD engineer, I want to programmatically submit test results to Paw-Hub so that test execution data is automatically captured and available for analysis.

## Acceptance Criteria

### 1. Test Execution Creation Endpoint
- [ ] POST `/api/v1/suites/{suiteId}/executions` endpoint implemented
- [ ] Accepts test execution metadata (timestamp, duration, environment, git info)
- [ ] Supports both individual and batch result submissions
- [ ] API key authentication supported for CI/CD integrations
- [ ] Returns execution ID for result submission
- [ ] Validates suite exists and user has access

### 2. Test Results Batch Submission
- [ ] POST `/api/v1/executions/{executionId}/results` accepts array of test results
- [ ] Each result includes: test name, status, duration, error details
- [ ] Supports up to 10,000 results per submission
- [ ] Partial failure handling (some results fail, others succeed)
- [ ] Async processing for large batches
- [ ] Returns job ID for tracking submission status

### 3. Result Format Support
- [ ] JUnit XML format parsing (Allure, TestNG, pytest)
- [ ] JSON format for custom frameworks
- [ ] CSV import for historical data
- [ ] Standardized internal format for storage
- [ ] Format validation and error reporting
- [ ] Automatic format detection

### 4. Test Result Data Mapping
- [ ] Test case name/identifier
- [ ] Execution status (passed, failed, skipped, error)
- [ ] Execution duration (milliseconds)
- [ ] Error messages and stack traces
- [ ] Standard output/logs
- [ ] Error type classification
- [ ] Retry information

### 5. Execution Metadata Capture
- [ ] Execution timestamp (UTC)
- [ ] Environment name (dev, staging, prod, etc.)
- [ ] Git commit hash
- [ ] Git branch name
- [ ] Git author
- [ ] Build number/ID
- [ ] CI/CD platform identifier
- [ ] Test framework/runner version

### 6. Aggregation & Normalization
- [ ] Calculate execution statistics (pass/fail/skip counts, pass rate)
- [ ] Generate execution summary
- [ ] Normalize test names across runs
- [ ] Deduplicate results
- [ ] Handle concurrent result submissions
- [ ] Transaction consistency

### 7. Data Validation
- [ ] Validate all required fields present
- [ ] Validate data types and formats
- [ ] Validate test names length (max 500 chars)
- [ ] Validate error messages (max 10KB)
- [ ] Reject invalid or malformed data
- [ ] Provide detailed validation error messages

### 8. API Key Management
- [ ] API key unique identification
- [ ] API key rate limiting (per key limits)
- [ ] API key rotation capability
- [ ] API key scope/permission management
- [ ] API key audit logging
- [ ] Expired key rejection

### 9. Error Handling & Retries
- [ ] Idempotent submission (same data twice returns same result)
- [ ] Retry logic for transient failures
- [ ] Clear error messages for client troubleshooting
- [ ] HTTP 429 for rate limit exceeded
- [ ] HTTP 413 for payload too large
- [ ] Meaningful validation error responses

## Technical Details

### API Endpoints

```
POST /api/v1/suites/{suiteId}/executions
  - Headers: Authorization or X-API-Key
  - Body: { started_at, ended_at, environment, git_commit, git_branch }
  - Returns: { execution_id, upload_token }

POST /api/v1/executions/{executionId}/results
  - Headers: Authorization or X-API-Key
  - Body: { results: [], format: "junit|json|csv" }
  - Returns: { success, job_id, imported_count, failed_count }

GET /api/v1/jobs/{jobId}
  - Returns: Job status and import statistics
```

### Execution Payload Example

```json
{
  "started_at": "2026-03-28T10:00:00Z",
  "ended_at": "2026-03-28T10:15:30Z",
  "environment": "staging",
  "git_commit": "a1b2c3d4e5f6g7h8",
  "git_branch": "main",
  "git_author": "john@example.com",
  "build_number": "12345",
  "ci_platform": "github-actions"
}
```

### Test Results Payload Example

```json
{
  "format": "json",
  "results": [
    {
      "name": "TestUserLogin",
      "class_name": "com.example.tests.AuthTest",
      "status": "passed",
      "duration_ms": 1234,
      "tags": ["regression", "smoke"]
    },
    {
      "name": "TestPaymentProcessing",
      "class_name": "com.example.tests.PaymentTest",
      "status": "failed",
      "duration_ms": 5678,
      "error_message": "AssertionError: Expected 200 but got 500",
      "stack_trace": "...",
      "tags": ["payment", "critical"]
    }
  ]
}
```

### JUnit XML to Internal Format Mapping

```
JUnit XML Element → Internal Field
<testcase name>     → test_name
<testcase classname> → class_name  
<testcase time>     → duration_ms (* 1000)
<failure> or <error> → status: "failed"
<failure message>   → error_message
<failure text>      → stack_trace
<skipped>          → status: "skipped"
```

## Dependencies
- Story 1: Project Initialization
- Story 2: Database Schema Setup
- Story 4: User Login Authentication
- Story 6: Test Suite Management

## Related Stories
- Story 8: Dashboard Basic Overview
- Story 9: Test Result Details View
- Story 14: CI/CD Pipeline Integration

## Definition of Done
- [ ] All ingestion endpoints implemented and tested
- [ ] Multiple format support working
- [ ] Batch processing implemented with async job tracking
- [ ] API key authentication working
- [ ] Rate limiting implemented
- [ ] Data transformation and validation complete
- [ ] Unit tests (>90% coverage)
- [ ] Integration tests with sample CI/CD data
- [ ] Performance tested with 10,000 results
- [ ] API documentation with examples
- [ ] CLI client for manual testing provided

## Performance Requirements
- [ ] Ingest 10,000 results in <5 seconds
- [ ] API response time <500ms for batch submission
- [ ] Job status query returns within 100ms
- [ ] Support concurrent submissions from multiple CI/CD systems

## Testing Checklist
- [ ] Valid JUnit XML parsed correctly
- [ ] Valid JSON format accepted
- [ ] Invalid formats rejected with clear errors
- [ ] Large batch (10,000) processed successfully
- [ ] Partial failures handled gracefully
- [ ] Duplicate submissions idempotent
- [ ] API key authentication works
- [ ] Rate limiting enforced
- [ ] Test result aggregation accurate
- [ ] All metadata fields captured correctly
- [ ] Concurrent submissions don't conflict
- [ ] Performance meets requirements

## Implementation Notes
- Use message queue (RabbitMQ/Kafka) for async processing
- Implement progress tracking for large jobs
- Cache format parsing results
- Consider compression for large payloads
- Implement checksum validation for data integrity

## Security Considerations
- Validate API key before processing (rate limit early)
- Prevent XXE attacks in XML parsing
- Validate payload size limits
- Sanitize error messages (no sensitive data leaks)
- Implement CORS properly
- Use HTTPS only

## Notes
- May add GraphQL endpoint in future
- Consider real-time result streaming via WebSocket
- May add result transformation rules (custom mapping)
- Consider machine learning-based issue categorization in future
