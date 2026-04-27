# Paw-Hub v1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a full-stack test result review application — Spring Boot backend for collection/analysis/triage + Next.js frontend.

**Architecture:** Modular monolith — single Spring Boot app with internal module boundaries + Next.js frontend. H2 embedded for v1.

**Tech Stack:** Java 21, Spring Boot 3.4, Spring Security + JWT, JPA/Hibernate, H2, Next.js 15, TypeScript, Recharts, Tailwind CSS, Docker Compose

**Total tasks:** 56

---

## File Structure

```
paw-hub/
├── docker-compose.yml
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/java/com/pawhub/
│       │   ├── PawHubApplication.java
│       │   ├── common/
│       │   │   ├── exception/PawHubException.java
│       │   │   ├── exception/GlobalExceptionHandler.java
│       │   │   └── dto/ApiResponse.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   ├── CorsConfig.java
│       │   │   └── AsyncConfig.java
│       │   ├── module/
│       │   │   ├── auth/
│       │   │   │   ├── entity/{User,Organization,Team,Project,Membership,MembershipRole}.java
│       │   │   │   ├── repository/{User,Organization,Team,Project,Membership}Repository.java
│       │   │   │   ├── service/{AuthService,TenantService}.java
│       │   │   │   ├── dto/{LoginRequest,RegisterRequest,AuthResponse}.java
│       │   │   │   └── controller/AuthController.java
│       │   │   ├── collection/
│       │   │   │   ├── entity/{TestRun,TestExecution,TestStatus}.java
│       │   │   │   ├── repository/{TestRun,TestExecution}Repository.java
│       │   │   │   ├── service/CollectionService.java
│       │   │   │   ├── dto/{TestResultUploadRequest,TestRunResponse,TestExecutionResponse}.java
│       │   │   │   ├── parser/JUnitXmlParser.java
│       │   │   │   ├── event/TestResultCollectedEvent.java
│       │   │   │   └── controller/CollectionController.java
│       │   │   ├── analysis/
│       │   │   │   ├── entity/{TrendSnapshot,FlakyTestRecord,FailureCluster}.java
│       │   │   │   ├── repository/{TrendSnapshot,FlakyTestRecord,FailureCluster}Repository.java
│       │   │   │   ├── service/{TrendAnalysis,RegressionDetection,FlakyDetection,FailureClustering}Service.java
│       │   │   │   ├── event/AnalysisListener.java
│       │   │   │   ├── dto/{TrendResponse,FlakyTestResponse,FailureClusterResponse}.java
│       │   │   │   └── controller/AnalysisController.java
│       │   │   ├── triage/
│       │   │   │   ├── entity/FailureTriage.java
│       │   │   │   ├── repository/FailureTriageRepository.java
│       │   │   │   ├── service/TriageService.java
│       │   │   │   ├── dto/{TriageRequest,TriageResponse,TriageSummaryResponse}.java
│       │   │   │   └── controller/TriageController.java
│       │   │   └── reporting/
│       │   │       ├── service/{SummaryService,ExportService}.java
│       │   │       ├── dto/SummaryResponse.java
│       │   │       └── controller/ReportingController.java
│       │   └── infrastructure/
│       │       ├── jwt/JwtTokenProvider.java
│       │       └── jwt/JwtAuthenticationFilter.java
│       ├── main/resources/
│       │   ├── application.yml
│       │   └── application-prod.yml
│       └── test/java/com/pawhub/
│           └── module/{collection,analysis,triage,auth}/...
├── frontend/
│   ├── package.json
│   ├── tsconfig.json
│   ├── next.config.js
│   ├── tailwind.config.ts
│   ├── postcss.config.js
│   ├── Dockerfile
│   └── src/
│       ├── app/
│       │   ├── layout.tsx
│       │   ├── page.tsx
│       │   ├── login/page.tsx
│       │   ├── register/page.tsx
│       │   └── projects/[projectId]/
│       │       ├── page.tsx
│       │       ├── runs/[runId]/page.tsx
│       │       ├── trends/page.tsx
│       │       └── settings/page.tsx
│       ├── components/
│       │   ├── layout/{Navbar,Sidebar}.tsx
│       │   ├── dashboard/{KpiCards,PassRateChart,RecentRegressions,TriageBreakdown}.tsx
│       │   ├── explorer/{FilterBar,TestTable,TriageModal,ErrorDetail}.tsx
│       │   ├── trends/{EnvironmentTrendChart,DurationTrendChart,FlakyTable,ClusterTable}.tsx
│       │   ├── settings/{ProjectConfig,TeamList}.tsx
│       │   └── ui/Card.tsx
│       └── lib/{api,auth,types}.ts
```

---

## Phase 1: Project Scaffolding (Tasks 1-3)

### Task 1: Initialize Spring Boot Backend

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/Dockerfile`
- Create: `backend/src/main/java/com/pawhub/PawHubApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/application-prod.yml`

**Step 1: Create pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.2</version>
    </parent>
    <groupId>com.pawhub</groupId>
    <artifactId>paw-hub</artifactId>
    <version>0.1.0</version>
    <name>Paw-Hub</name>

    <properties>
        <java.version>21</java.version>
        <jjwt.version>0.12.6</jjwt.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**Step 2: Create PawHubApplication.java**

```java
package com.pawhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PawHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(PawHubApplication.class, args);
    }
}
```

**Step 3: Create application.yml**

```yaml
spring:
  application:
    name: paw-hub
  datasource:
    url: jdbc:h2:file:./data/pawhub;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect
    open-in-view: false
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

server:
  port: 8080

app:
  jwt:
    secret: paw-hub-dev-secret-change-in-production-must-be-256-bits
    expiration-ms: 86400000

analysis:
  flaky-threshold: 0.3
  regression-sigma: 2.0
  window-days: 30
  consecutive-pass-count: 5

logging:
  level:
    com.pawhub: DEBUG
```

**Step 4: Create application-prod.yml**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/pawhub
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME:pawhub}
    password: ${DB_PASSWORD:pawhub}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  h2:
    console:
      enabled: false
```

**Step 5: Create backend Dockerfile**

```dockerfile
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/paw-hub-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Step 6: Verify & Commit**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

```bash
git add backend/pom.xml backend/Dockerfile backend/src/main/java/com/pawhub/PawHubApplication.java backend/src/main/resources/
git commit -m "feat: scaffold Spring Boot backend with dependencies"
```

### Task 2: Initialize Next.js Frontend

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/tsconfig.json`
- Create: `frontend/next.config.js`
- Create: `frontend/tailwind.config.ts`
- Create: `frontend/postcss.config.js`
- Create: `frontend/Dockerfile`

**Step 1: Create package.json**

```json
{
  "name": "paw-hub-frontend",
  "version": "0.1.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint"
  },
  "dependencies": {
    "next": "^15.1.0",
    "react": "^19.0.0",
    "react-dom": "^19.0.0",
    "recharts": "^2.15.0"
  },
  "devDependencies": {
    "@types/node": "^22.0.0",
    "@types/react": "^19.0.0",
    "@types/react-dom": "^19.0.0",
    "typescript": "^5.7.0",
    "tailwindcss": "^4.0.0",
    "@tailwindcss/postcss": "^4.0.0",
    "postcss": "^8.5.0"
  }
}
```

**Step 2: Create tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2017",
    "lib": ["dom", "dom.iterable", "esnext"],
    "allowJs": true,
    "skipLibCheck": true,
    "strict": true,
    "noEmit": true,
    "esModuleInterop": true,
    "module": "esnext",
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "jsx": "preserve",
    "incremental": true,
    "plugins": [{"name": "next"}],
    "paths": {"@/*": ["./src/*"]}
  },
  "include": ["next-env.d.ts", "**/*.ts", "**/*.tsx", ".next/types/**/*.ts"],
  "exclude": ["node_modules"]
}
```

**Step 3: Create next.config.js**

```js
/** @type {import('next').NextConfig} */
const nextConfig = { output: 'standalone' };
module.exports = nextConfig;
```

**Step 4: Create Tailwind config**

`frontend/tailwind.config.ts`:
```ts
import type { Config } from "tailwindcss";

export default {
  content: ["./src/**/*.{js,ts,jsx,tsx,mdx}"],
  theme: { extend: {} },
  plugins: [],
} satisfies Config;
```

`frontend/postcss.config.js`:
```js
module.exports = {
  plugins: { "@tailwindcss/postcss": {} },
};
```

**Step 5: Create frontend Dockerfile**

```dockerfile
FROM node:22-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:22-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
EXPOSE 3000
CMD ["node", "server.js"]
```

**Step 6: Install dependencies**

Run: `cd frontend && npm install`
Expected: installs successfully

**Step 7: Commit**

```bash
git add frontend/package.json frontend/tsconfig.json frontend/next.config.js frontend/tailwind.config.ts frontend/postcss.config.js frontend/Dockerfile
git commit -m "feat: scaffold Next.js frontend project"
```

### Task 3: Create Docker Compose

**Files:**
- Create: `docker-compose.yml`

```yaml
services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: default
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
    environment:
      NEXT_PUBLIC_API_URL: http://backend:8080
    depends_on:
      - backend
```

```bash
git add docker-compose.yml
git commit -m "feat: add Docker Compose configuration"
```

---

## Phase 2: Backend Core Infrastructure (Tasks 4-10)

### Task 4: Common Exception Handling & API Response

```java
// backend/src/main/java/com/pawhub/common/dto/ApiResponse.java
package com.pawhub.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String message, T data) {
    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, null, data); }
    public static <T> ApiResponse<T> ok() { return new ApiResponse<>(true, null, null); }
    public static <T> ApiResponse<T> error(String message) { return new ApiResponse<>(false, message, null); }
}
```

```java
// backend/src/main/java/com/pawhub/common/exception/PawHubException.java
package com.pawhub.common.exception;

import org.springframework.http.HttpStatus;

public class PawHubException extends RuntimeException {
    private final HttpStatus status;
    public PawHubException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
    public HttpStatus getStatus() { return status; }
}
```

```java
// backend/src/main/java/com/pawhub/common/exception/GlobalExceptionHandler.java
package com.pawhub.common.exception;

import com.pawhub.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PawHubException.class)
    public ResponseEntity<ApiResponse<Void>> handle(PawHubException e) {
        return ResponseEntity.status(e.getStatus()).body(ApiResponse.error(e.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handle(Exception e) {
        return ResponseEntity.internalServerError().body(ApiResponse.error("Internal server error"));
    }
}
```

```bash
git add backend/src/main/java/com/pawhub/common/
git commit -m "feat: add common exception handling and API response DTO"
```

### Task 5: JWT Token Provider & Filter

```java
// backend/src/main/java/com/pawhub/infrastructure/jwt/JwtTokenProvider.java
package com.pawhub.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public Long getUserId(Claims claims) { return Long.parseLong(claims.getSubject()); }
}
```

```java
// backend/src/main/java/com/pawhub/infrastructure/jwt/JwtAuthenticationFilter.java
package com.pawhub.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            try {
                Claims claims = jwtTokenProvider.validateToken(bearer.substring(7));
                Long userId = jwtTokenProvider.getUserId(claims);
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {}
        }
        chain.doFilter(request, response);
    }
}
```

```bash
git add backend/src/main/java/com/pawhub/infrastructure/
git commit -m "feat: add JWT token provider and authentication filter"
```

### Task 6: Security, CORS, and Async Config

```java
// backend/src/main/java/com/pawhub/config/SecurityConfig.java
package com.pawhub.config;

import com.pawhub.infrastructure.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    public SecurityConfig(JwtAuthenticationFilter jwtFilter) { this.jwtFilter = jwtFilter; }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/actuator/health", "/h2-console/**").permitAll()
                .requestMatchers("/api/v1/projects/*/test-results").permitAll()
                .anyRequest().authenticated())
            .headers(h -> h.frameOptions(fo -> fo.sameOrigin()))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
```

```java
// backend/src/main/java/com/pawhub/config/CorsConfig.java
package com.pawhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOriginPatterns(List.of("*"));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return new CorsFilter(s);
    }
}
```

```java
// backend/src/main/java/com/pawhub/config/AsyncConfig.java
package com.pawhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean("analysisExecutor")
    public Executor analysisExecutor() {
        ThreadPoolTaskExecutor e = new ThreadPoolTaskExecutor();
        e.setCorePoolSize(2);
        e.setMaxPoolSize(4);
        e.setQueueCapacity(100);
        e.setThreadNamePrefix("analysis-");
        e.initialize();
        return e;
    }
}
```

```bash
git add backend/src/main/java/com/pawhub/config/
git commit -m "feat: add security, CORS, and async config"
```

### Task 7: Auth Entities

```java
// backend/src/main/java/com/pawhub/module/auth/entity/Organization.java
package com.pawhub.module.auth.entity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "organizations")
public class Organization {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    public Organization() {}
    public Organization(String name) { this.name = name; }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public Instant getCreatedAt() { return createdAt; }
}
```

```java
// backend/src/main/java/com/pawhub/module/auth/entity/Team.java
package com.pawhub.module.auth.entity;
import jakarta.persistence.*;

@Entity @Table(name = "teams")
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String name;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;
    public Team() {}
    public Team(String name, Organization org) { this.name = name; this.organization = org; }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization org) { this.organization = org; }
}
```

```java
// backend/src/main/java/com/pawhub/module/auth/entity/MembershipRole.java
package com.pawhub.module.auth.entity;
public enum MembershipRole { ADMIN, QA, VIEWER }
```

```java
// backend/src/main/java/com/pawhub/module/auth/entity/Project.java
package com.pawhub.module.auth.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "projects")
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String name;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    @Column(nullable = false, unique = true)
    private String apiKey = "sk-" + UUID.randomUUID().toString().substring(0, 20);
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    public Project() {}
    public Project(String name, Team team) { this.name = name; this.team = team; }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public Team getTeam() { return team; } public void setTeam(Team t) { this.team = t; }
    public String getApiKey() { return apiKey; }
    public Instant getCreatedAt() { return createdAt; }
}
```

```java
// backend/src/main/java/com/pawhub/module/auth/entity/User.java
package com.pawhub.module.auth.entity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true) private String username;
    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String passwordHash;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    public User() {}
    public User(String username, String email, String passwordHash) {
        this.username = username; this.email = email; this.passwordHash = passwordHash;
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; } public void setUsername(String u) { this.username = u; }
    public String getEmail() { return email; } public void setEmail(String e) { this.email = e; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String h) { this.passwordHash = h; }
    public Instant getCreatedAt() { return createdAt; }
}
```

```java
// backend/src/main/java/com/pawhub/module/auth/entity/Membership.java
package com.pawhub.module.auth.entity;
import jakarta.persistence.*;

@Entity @Table(name = "memberships", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "team_id"}))
public class Membership {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private MembershipRole role;
    public Membership() {}
    public Membership(User u, Team t, MembershipRole r) { this.user = u; this.team = t; this.role = r; }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public User getUser() { return user; } public void setUser(User u) { this.user = u; }
    public Team getTeam() { return team; } public void setTeam(Team t) { this.team = t; }
    public MembershipRole getRole() { return role; } public void setRole(MembershipRole r) { this.role = r; }
}
```

```bash
git add backend/src/main/java/com/pawhub/module/auth/entity/
git commit -m "feat: add auth entities — User, Organization, Team, Project, Membership"
```

### Task 8: Auth Repositories

```java
// backend/src/main/java/com/pawhub/module/auth/repository/UserRepository.java
package com.pawhub.module.auth.repository;
import com.pawhub.module.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}

// OrganizationRepository.java
package com.pawhub.module.auth.repository;
import com.pawhub.module.auth.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrganizationRepository extends JpaRepository<Organization, Long> {}

// TeamRepository.java
package com.pawhub.module.auth.repository;
import com.pawhub.module.auth.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByOrganizationId(Long orgId);
}

// ProjectRepository.java
package com.pawhub.module.auth.repository;
import com.pawhub.module.auth.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByTeamId(Long teamId);
}

// MembershipRepository.java
package com.pawhub.module.auth.repository;
import com.pawhub.module.auth.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByUserId(Long userId);
    boolean existsByUserIdAndTeamId(Long userId, Long teamId);
}
```

```bash
git add backend/src/main/java/com/pawhub/module/auth/repository/
git commit -m "feat: add auth repositories"
```

### Task 9: Auth Service & Controller

```java
// backend/src/main/java/com/pawhub/module/auth/dto/LoginRequest.java
package com.pawhub.module.auth.dto;
import jakarta.validation.constraints.NotBlank;
public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

// RegisterRequest.java
package com.pawhub.module.auth.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
public record RegisterRequest(@NotBlank String username, @NotBlank @Email String email, @NotBlank String password) {}

// AuthResponse.java
package com.pawhub.module.auth.dto;
public record AuthResponse(String token, Long userId, String username) {}
```

```java
// backend/src/main/java/com/pawhub/module/auth/service/AuthService.java
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
```

```java
// backend/src/main/java/com/pawhub/module/auth/controller/AuthController.java
package com.pawhub.module.auth.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.module.auth.dto.AuthResponse;
import com.pawhub.module.auth.dto.LoginRequest;
import com.pawhub.module.auth.dto.RegisterRequest;
import com.pawhub.module.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService s) { this.authService = s; }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest r) {
        return ApiResponse.ok(authService.register(r));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest r) {
        return ApiResponse.ok(authService.login(r));
    }
}
```

```bash
git add backend/src/main/java/com/pawhub/module/auth/
git commit -m "feat: add auth service and controller"
```

### Task 10: Tenant Management Service

```java
// backend/src/main/java/com/pawhub/module/auth/service/TenantService.java
package com.pawhub.module.auth.service;

import com.pawhub.common.exception.PawHubException;
import com.pawhub.module.auth.entity.*;
import com.pawhub.module.auth.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {
    private final OrganizationRepository orgRepo;
    private final TeamRepository teamRepo;
    private final ProjectRepository projectRepo;
    private final MembershipRepository membershipRepo;
    private final UserRepository userRepo;

    public TenantService(OrganizationRepository o, TeamRepository t, ProjectRepository p,
                         MembershipRepository m, UserRepository u) {
        this.orgRepo = o; this.teamRepo = t; this.projectRepo = p;
        this.membershipRepo = m; this.userRepo = u;
    }

    public Organization createOrg(String name) { return orgRepo.save(new Organization(name)); }

    public Team createTeam(Long orgId, String name) {
        Organization org = orgRepo.findById(orgId)
            .orElseThrow(() -> new PawHubException("Org not found", HttpStatus.NOT_FOUND));
        return teamRepo.save(new Team(name, org));
    }

    @Transactional
    public Project createProject(Long teamId, String name, Long userId) {
        Team team = teamRepo.findById(teamId)
            .orElseThrow(() -> new PawHubException("Team not found", HttpStatus.NOT_FOUND));
        if (!membershipRepo.existsByUserIdAndTeamId(userId, teamId))
            throw new PawHubException("Not a team member", HttpStatus.FORBIDDEN);
        return projectRepo.save(new Project(name, team));
    }

    @Transactional
    public Membership addMember(Long teamId, Long userId, MembershipRole role) {
        if (membershipRepo.existsByUserIdAndTeamId(userId, teamId))
            throw new PawHubException("Already a member", HttpStatus.CONFLICT);
        User u = userRepo.findById(userId)
            .orElseThrow(() -> new PawHubException("User not found", HttpStatus.NOT_FOUND));
        Team t = teamRepo.findById(teamId)
            .orElseThrow(() -> new PawHubException("Team not found", HttpStatus.NOT_FOUND));
        return membershipRepo.save(new Membership(u, t, role));
    }
}
```

```bash
git add backend/src/main/java/com/pawhub/module/auth/service/TenantService.java
git commit -m "feat: add tenant management service"
```
