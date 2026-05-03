package com.pawhub.module.auth.service;

import com.pawhub.common.exception.PawHubException;
import com.pawhub.infrastructure.jwt.JwtTokenProvider;
import com.pawhub.module.auth.dto.AuthResponse;
import com.pawhub.module.auth.dto.LoginRequest;
import com.pawhub.module.auth.dto.RegisterRequest;
import com.pawhub.module.auth.entity.User;
import com.pawhub.module.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwt;

    public AuthService(UserRepository ur, PasswordEncoder pe, JwtTokenProvider j) {
        this.userRepo = ur; this.passwordEncoder = pe; this.jwt = j;
    }

    @Transactional
    public AuthResponse register(RegisterRequest r) {
        if (userRepo.existsByUsername(r.username()))
            throw new PawHubException("Username already taken", HttpStatus.CONFLICT);
        if (userRepo.existsByEmail(r.email()))
            throw new PawHubException("Email already registered", HttpStatus.CONFLICT);
        User u = new User(r.username(), r.email(), passwordEncoder.encode(r.password()));
        u = userRepo.save(u);
        return new AuthResponse(jwt.generateToken(u.getId(), u.getUsername()), u.getId(), u.getUsername());
    }

    public AuthResponse login(LoginRequest r) {
        User u = userRepo.findByUsername(r.username())
            .orElseThrow(() -> new PawHubException("Invalid credentials", HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(r.password(), u.getPasswordHash()))
            throw new PawHubException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        return new AuthResponse(jwt.generateToken(u.getId(), u.getUsername()), u.getId(), u.getUsername());
    }
}
