package com.pawhub.module.analysis.dto;
import java.time.Instant;

public record FlakyTestResponse(String testCaseKey, double flakyScore, int transitionCount, int retryPassCount, Instant lastDetectedAt) {}
