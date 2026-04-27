package com.pawhub.module.collection.dto;

import com.pawhub.module.collection.entity.TestExecution;
import com.pawhub.module.collection.entity.TestStatus;

public record TestExecutionResponse(Long id, int attempt, String suiteName, String className,
    String testName, String caseNumber, TestStatus status, long durationMs,
    String errorMessage, String errorType, String stackTrace) {
    public static TestExecutionResponse from(TestExecution te) {
        return new TestExecutionResponse(te.getId(), te.getAttempt(), te.getSuiteName(),
            te.getClassName(), te.getTestName(), te.getCaseNumber(), te.getStatus(),
            te.getDurationMs(), te.getErrorMessage(), te.getErrorType(), te.getStackTrace());
    }
}
