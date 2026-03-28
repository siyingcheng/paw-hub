package com.pawhub.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health Check Controller
 *
 * Provides health check endpoints for monitoring and load balancing.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("service", "Paw-Hub Backend");
        response.put("version", "1.0.0");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "READY");
        response.put("message", "Application is ready to serve traffic");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
