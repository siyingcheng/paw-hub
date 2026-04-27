package com.pawhub.module.auth.dto;

public record AuthResponse(String token, Long userId, String username) {}
