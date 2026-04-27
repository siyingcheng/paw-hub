package com.pawhub.module.analysis.dto;
import java.time.Instant;

public record FailureClusterResponse(String clusterKey, String representativeError, int occurrenceCount, Instant firstSeen, Instant lastSeen) {}
