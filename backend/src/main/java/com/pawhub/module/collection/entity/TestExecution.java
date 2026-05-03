package com.pawhub.module.collection.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

@Entity @Table(name = "test_executions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"test_run_id","suite_name","class_name","test_name","attempt"}))
public class TestExecution {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "test_run_id", nullable = false)
    private TestRun testRun;
    @Min(1) @Column(nullable = false) private int attempt = 1;
    @Column(nullable = false) private String suiteName;
    @Column(nullable = false) private String className;
    @Column(nullable = false) private String testName;
    private String caseNumber;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TestStatus status;
    private long durationMs;
    @Column(length = 4000) private String errorMessage;
    @Lob @Column(columnDefinition = "CLOB") private String stackTrace;
    private String errorType;
    public TestExecution() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public TestRun getTestRun() { return testRun; } public void setTestRun(TestRun t) { this.testRun = t; }
    public int getAttempt() { return attempt; } public void setAttempt(int n) { this.attempt = n; }
    public String getSuiteName() { return suiteName; } public void setSuiteName(String s) { this.suiteName = s; }
    public String getClassName() { return className; } public void setClassName(String s) { this.className = s; }
    public String getTestName() { return testName; } public void setTestName(String s) { this.testName = s; }
    public String getCaseNumber() { return caseNumber; } public void setCaseNumber(String s) { this.caseNumber = s; }
    public TestStatus getStatus() { return status; } public void setStatus(TestStatus s) { this.status = s; }
    public long getDurationMs() { return durationMs; } public void setDurationMs(long n) { this.durationMs = n; }
    public String getErrorMessage() { return errorMessage; } public void setErrorMessage(String s) { this.errorMessage = s; }
    public String getStackTrace() { return stackTrace; } public void setStackTrace(String s) { this.stackTrace = s; }
    public String getErrorType() { return errorType; } public void setErrorType(String s) { this.errorType = s; }
}
