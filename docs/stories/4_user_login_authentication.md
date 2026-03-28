# Story 4: User Login Authentication

**Sprint**: 2  
**Priority**: P0 (Critical)  
**Story Points**: 8  
**Status**: Not Started

## Summary

Implement user login authentication with JWT tokens, session management, and secure token refresh mechanisms.

## Description

As a registered user, I want to log in with my credentials so that I can access the Paw-Hub platform and manage test results.

## Acceptance Criteria

### 1. Login API Endpoint
- [ ] POST `/api/v1/auth/login` endpoint implemented
- [ ] Accepts email and password credentials
- [ ] Returns access token and refresh token on success
- [ ] Returns appropriate error on failed authentication
- [ ] Rate limiting prevents brute force attacks (5 attempts per 15 minutes per IP)
- [ ] Email verification status checked before login

### 2. Token Management
- [ ] Access token generated with 1-hour expiration
- [ ] Refresh token generated with 7-day expiration
- [ ] Tokens are JWT format with proper claims (user_id, email, role)
- [ ] Tokens signed with secure key stored in environment
- [ ] Token structure includes: iss, aud, exp, iat, sub, email, role
- [ ] Refresh token stored in database for revocation capability

### 3. Session Management
- [ ] Session created in Redis upon successful login
- [ ] Session timeout after 30 minutes of inactivity
- [ ] Multiple concurrent sessions supported per user
- [ ] Session invalidation on logout
- [ ] Device/browser tracking for security

### 4. Token Refresh Endpoint
- [ ] POST `/api/v1/auth/refresh` endpoint implemented
- [ ] Validates refresh token before issuing new access token
- [ ] Invalid or expired refresh tokens rejected
- [ ] New access token issued with same claims
- [ ] Refresh token can be rotated (new one issued with new access token)
- [ ] Rate limiting on refresh token usage

### 5. Logout Functionality
- [ ] POST `/api/v1/auth/logout` endpoint implemented
- [ ] Invalidates session in Redis
- [ ] Adds token to blacklist/revocation list
- [ ] Returns success response
- [ ] Frontend clears stored tokens

### 6. Frontend Login Form
- [ ] Login page UI created at `/login`
- [ ] Form fields for email and password
- [ ] Remember me checkbox (optional)
- [ ] Error messages displayed for failed login
- [ ] Loading state during authentication
- [ ] Redirect to dashboard on successful login
- [ ] Forgot password link (Story 12 scope)
- [ ] Links to registration page

### 7. Protected Routes
- [ ] Authentication middleware implemented
- [ ] Requests without valid token rejected with 401
- [ ] Expired tokens trigger refresh attempt
- [ ] Failed refresh redirects to login
- [ ] User role extracted from token for authorization

### 8. Security Measures
- [ ] Passwords never transmitted in plain text (HTTPS only)
- [ ] Failed login attempts logged
- [ ] Account lockout after 5 failed attempts (15 minutes)
- [ ] IP-based rate limiting implemented
- [ ] CORS properly configured for token endpoints
- [ ] CSRF protection on login form

## Technical Details

### Login API Endpoint

```
POST /api/v1/auth/login
Content-Type: application/json

Request:
{
  "email": "user@example.com",
  "password": "SecurePass@123"
}

Success Response (200):
{
  "success": true,
  "data": {
    "access_token": "eyJhbGc...",
    "refresh_token": "eyJhbGc...",
    "token_type": "Bearer",
    "expires_in": 3600,
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "username": "johndoe",
      "full_name": "John Doe",
      "role": "user"
    }
  },
  "timestamp": "2026-03-28T10:00:00Z"
}

Failed Response (401):
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "Invalid email or password"
  },
  "timestamp": "2026-03-28T10:00:00Z"
}
```

### JWT Payload Structure
```json
{
  "iss": "paw-hub",
  "aud": "paw-hub-api",
  "sub": "user-uuid",
  "email": "user@example.com",
  "username": "johndoe",
  "role": "user",
  "iat": 1713262800,
  "exp": 1713266400,
  "jti": "token-id"
}
```

### Refresh Token Flow
```
POST /api/v1/auth/refresh

Request:
{
  "refresh_token": "eyJhbGc..."
}

Response:
{
  "success": true,
  "data": {
    "access_token": "eyJhbGc...",
    "refresh_token": "eyJhbGc...",
    "expires_in": 3600
  }
}
```

### Rate Limiting Configuration
- Failed login attempts: 5 per 15 minutes per IP
- Refresh attempts: 10 per hour per user
- Authentication endpoints: 100 per hour per IP

## Dependencies
- Story 1: Project Initialization
- Story 2: Database Schema Setup
- Story 3: User Registration System

## Related Stories
- Story 5: Project Management
- Story 7: User Profile & Settings (future)

## Definition of Done
- [ ] Login API fully implemented and tested
- [ ] Frontend form created with validation
- [ ] Token generation and validation working
- [ ] Session management in Redis working
- [ ] Logout functionality working
- [ ] Protected route middleware implemented
- [ ] Unit tests written (>90% coverage)
- [ ] Integration tests for auth flow
- [ ] No security vulnerabilities (OWASP compliance)
- [ ] Documentation updated

## Security Considerations
- Use HS256 or RS256 algorithms (avoid none)
- Store secret key securely (environment variable, vault)
- Validate token signature before trusting claims
- Prevent token leakage (secure storage in HttpOnly cookie)
- Implement token rotation strategy
- Monitor for suspicious authentication patterns

## Testing Checklist
- [ ] Valid credentials accepted
- [ ] Invalid credentials rejected
- [ ] Email not verified rejected
- [ ] Locked accounts cannot login
- [ ] Token refresh works correctly
- [ ] Expired tokens rejected
- [ ] Logout invalidates session
- [ ] Rate limiting prevents brute force
- [ ] Multiple simultaneous sessions work
- [ ] Cross-origin requests properly handled

## Notes
- Consider implementing biometric/2FA in future
- May add login audit trail and IP logging
- Consider passwordless authentication options for v2
