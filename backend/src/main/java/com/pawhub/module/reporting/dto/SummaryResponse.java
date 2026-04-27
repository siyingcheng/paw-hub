package com.pawhub.module.reporting.dto;

import java.util.List;

public record SummaryResponse(long totalRuns, double overallPassRate, long totalFailures,
    TopFailure topFailure, List<FlakySummary> topFlakyTests) {}
