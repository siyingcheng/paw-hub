package com.pawhub.module.reporting.dto;

import java.util.List;

public record SummaryResponse(long totalRuns, double overallPassRate, long totalFailures,
    TopFailure topFailure, List<FlakySummary> topFlakyTests) {}

public record TopFailure(String testName, String errorMessage, long failCount) {}

public record FlakySummary(String testCaseKey, double flakyScore) {}
