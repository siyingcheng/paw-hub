package com.pawhub.module.analysis.entity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "failure_clusters", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "cluster_key"}))
public class FailureCluster {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long projectId;
    @Column(nullable = false) private String clusterKey;
    @Column(length = 1000) private String representativeError;
    private int occurrenceCount;
    private Instant firstSeen;
    private Instant lastSeen;
    public FailureCluster() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getProjectId() { return projectId; } public void setProjectId(Long id) { this.projectId = id; }
    public String getClusterKey() { return clusterKey; } public void setClusterKey(String s) { this.clusterKey = s; }
    public String getRepresentativeError() { return representativeError; }
    public void setRepresentativeError(String s) { this.representativeError = s; }
    public int getOccurrenceCount() { return occurrenceCount; } public void setOccurrenceCount(int n) { this.occurrenceCount = n; }
    public Instant getFirstSeen() { return firstSeen; } public void setFirstSeen(Instant i) { this.firstSeen = i; }
    public Instant getLastSeen() { return lastSeen; } public void setLastSeen(Instant i) { this.lastSeen = i; }
}
