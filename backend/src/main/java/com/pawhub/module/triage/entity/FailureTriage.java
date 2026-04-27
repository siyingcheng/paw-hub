package com.pawhub.module.triage.entity;

import com.pawhub.module.collection.entity.TestExecution;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "failure_triages")
public class FailureTriage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "test_execution_id", unique = true, nullable = false)
    private TestExecution testExecution;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TriageStatus triageStatus = TriageStatus.UNTRIAGED;
    private String issueLink;
    @Column(length = 2000) private String comment;
    private Long annotatedBy;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public enum TriageStatus { UNTRIAGED, NEW_BUG, KNOWN_ISSUE, SCRIPT_ISSUE, DATA_ISSUE, ENV_ISSUE, CR, OTHER }

    public FailureTriage() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public TestExecution getTestExecution() { return testExecution; }
    public void setTestExecution(TestExecution e) { this.testExecution = e; }
    public TriageStatus getTriageStatus() { return triageStatus; }
    public void setTriageStatus(TriageStatus s) { this.triageStatus = s; }
    public String getIssueLink() { return issueLink; } public void setIssueLink(String s) { this.issueLink = s; }
    public String getComment() { return comment; } public void setComment(String s) { this.comment = s; }
    public Long getAnnotatedBy() { return annotatedBy; } public void setAnnotatedBy(Long id) { this.annotatedBy = id; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant i) { this.updatedAt = i; }
}
