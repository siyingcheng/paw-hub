package com.pawhub.module.triage.dto;

import com.pawhub.module.triage.entity.FailureTriage;
import java.time.Instant;

public record TriageResponse(Long id, Long testExecutionId, String triageStatus,
    String issueLink, String comment, Long annotatedBy, Instant createdAt, Instant updatedAt) {
    public static TriageResponse from(FailureTriage t) {
        return new TriageResponse(t.getId(), t.getTestExecution().getId(), t.getTriageStatus().name(),
            t.getIssueLink(), t.getComment(), t.getAnnotatedBy(), t.getCreatedAt(), t.getUpdatedAt());
    }
}
