package com.pawhub.module.collection.entity;

import com.pawhub.module.auth.entity.Project;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "test_runs")
public class TestRun {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    private String runIdentifier;
    private String branch;
    private String commitSha;
    private String triggeredBy;
    @Column(nullable = false) private String environment;
    private int totalCases;
    private int passed;
    private int failed;
    private int skipped;
    private int retried;
    private long durationMs;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TestStatus status;
    @Lob @Column(columnDefinition = "CLOB") private String rawXml;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    public TestRun() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Project getProject() { return project; } public void setProject(Project p) { this.project = p; }
    public String getRunIdentifier() { return runIdentifier; } public void setRunIdentifier(String s) { this.runIdentifier = s; }
    public String getBranch() { return branch; } public void setBranch(String s) { this.branch = s; }
    public String getCommitSha() { return commitSha; } public void setCommitSha(String s) { this.commitSha = s; }
    public String getTriggeredBy() { return triggeredBy; } public void setTriggeredBy(String s) { this.triggeredBy = s; }
    public String getEnvironment() { return environment; } public void setEnvironment(String s) { this.environment = s; }
    public int getTotalCases() { return totalCases; } public void setTotalCases(int n) { this.totalCases = n; }
    public int getPassed() { return passed; } public void setPassed(int n) { this.passed = n; }
    public int getFailed() { return failed; } public void setFailed(int n) { this.failed = n; }
    public int getSkipped() { return skipped; } public void setSkipped(int n) { this.skipped = n; }
    public int getRetried() { return retried; } public void setRetried(int n) { this.retried = n; }
    public long getDurationMs() { return durationMs; } public void setDurationMs(long n) { this.durationMs = n; }
    public TestStatus getStatus() { return status; } public void setStatus(TestStatus s) { this.status = s; }
    public String getRawXml() { return rawXml; } public void setRawXml(String s) { this.rawXml = s; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant i) { this.createdAt = i; }
}
