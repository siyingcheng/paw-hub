package com.pawhub.module.auth.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.common.exception.PawHubException;
import com.pawhub.module.auth.dto.AuthResponse;
import com.pawhub.module.auth.dto.LoginRequest;
import com.pawhub.module.auth.dto.MembershipResponse;
import com.pawhub.module.auth.dto.RegisterRequest;
import com.pawhub.module.auth.entity.User;
import com.pawhub.module.auth.repository.MembershipRepository;
import com.pawhub.module.auth.repository.UserRepository;
import com.pawhub.module.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final MembershipRepository membershipRepo;
    private final UserRepository userRepo;

    public AuthController(AuthService s, MembershipRepository m, UserRepository u) {
        this.authService = s; this.membershipRepo = m; this.userRepo = u;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest r) {
        return ApiResponse.ok(authService.register(r));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest r) {
        return ApiResponse.ok(authService.login(r));
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new PawHubException("User not found", HttpStatus.NOT_FOUND));
        var memberships = membershipRepo.findByUserId(userId).stream()
            .map(MembershipResponse::from).toList();
        return ApiResponse.ok(new MeResponse(user.getId(), user.getUsername(), user.getEmail(), memberships));
    }

    public record MeResponse(Long id, String username, String email, List<MembershipResponse> memberships) {}
}
