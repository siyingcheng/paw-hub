package com.pawhub.module.auth.dto;

public record TeamMemberResponse(Long userId, String username, String email, String role) {}
