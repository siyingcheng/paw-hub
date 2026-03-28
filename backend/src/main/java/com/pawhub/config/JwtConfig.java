package com.pawhub.config;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Configuration for Paw-Hub
 *
 * Manages JWT token generation and validation settings.
 * Secrets should be loaded from environment variables in production.
 */
@Configuration
public class JwtConfig {

    @Value("${app.jwt.secret:dev-secret-key-change-in-production-12345678901234567890}")
    private String secret;

    @Value("${app.jwt.expiration:86400000}")
    private Long expiration;

    @Value("${app.jwt.issuer-uri:http://localhost:8080}")
    private String issuerUri;

    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Long getExpiration() {
        return expiration;
    }

    public String getIssuerUri() {
        return issuerUri;
    }
}
