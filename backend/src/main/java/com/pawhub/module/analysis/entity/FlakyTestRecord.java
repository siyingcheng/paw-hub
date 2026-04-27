package com.pawhub.module.analysis.entity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "flaky_test_records")
public class FlakyTestRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String testCaseKey;
    @Column(nullable = false) private Long projectId;
    private double flakyScore;
    private int transitionCount;
    private int retryPassCount;
    private Instant lastDetectedAt;
    public FlakyTestRecord() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getTestCaseKey() { return testCaseKey; } public void setTestCaseKey(String s) { this.testCaseKey = s; }
    public Long getProjectId() { return projectId; } public void setProjectId(Long id) { this.projectId = id; }
    public double getFlakyScore() { return flakyScore; } public void setFlakyScore(double d) { this.flakyScore = d; }
    public int getTransitionCount() { return transitionCount; } public void setTransitionCount(int n) { this.transitionCount = n; }
    public int getRetryPassCount() { return retryPassCount; } public void setRetryPassCount(int n) { this.retryPassCount = n; }
    public Instant getLastDetectedAt() { return lastDetectedAt; } public void setLastDetectedAt(Instant i) { this.lastDetectedAt = i; }
}
