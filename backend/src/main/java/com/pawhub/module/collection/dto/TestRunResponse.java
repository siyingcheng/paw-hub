package com.pawhub.module.collection.dto;

import com.pawhub.module.collection.entity.TestRun;
import com.pawhub.module.collection.entity.TestStatus;
import java.time.Instant;

public record TestRunResponse(Long id, Long projectId, String runIdentifier, String branch,
    String commitSha, String triggeredBy, String environment, int totalCases, int passed,
    int failed, int skipped, long durationMs, TestStatus status, Instant createdAt) {
    public static TestRunResponse from(TestRun tr) {
        return new TestRunResponse(tr.getId(), tr.getProject().getId(), tr.getRunIdentifier(),
            tr.getBranch(), tr.getCommitSha(), tr.getTriggeredBy(), tr.getEnvironment(),
            tr.getTotalCases(), tr.getPassed(), tr.getFailed(), tr.getSkipped(),
            tr.getDurationMs(), tr.getStatus(), tr.getCreatedAt());
    }
}
