package com.pawhub.module.collection.dto;

import com.pawhub.module.collection.entity.TestRun;
import com.pawhub.module.collection.entity.TestStatus;
import java.time.Instant;
import java.util.List;

public record TestRunDetailResponse(
    Long id, Long projectId, String runIdentifier,
    String branch, String commitSha, String triggeredBy,
    String environment, int totalCases, int passed,
    int failed, int skipped, long durationMs,
    TestStatus status, Instant createdAt,
    List<TestExecutionResponse> executions
) {
    public static TestRunDetailResponse from(TestRun tr, List<TestExecutionResponse> execs) {
        return new TestRunDetailResponse(
            tr.getId(), tr.getProject().getId(), tr.getRunIdentifier(),
            tr.getBranch(), tr.getCommitSha(), tr.getTriggeredBy(),
            tr.getEnvironment(), tr.getTotalCases(), tr.getPassed(),
            tr.getFailed(), tr.getSkipped(), tr.getDurationMs(),
            tr.getStatus(), tr.getCreatedAt(), execs
        );
    }
}
