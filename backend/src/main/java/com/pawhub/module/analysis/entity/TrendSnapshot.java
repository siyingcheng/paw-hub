package com.pawhub.module.analysis.entity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity @Table(name = "trend_snapshots",
    uniqueConstraints = @UniqueConstraint(columnNames = {"project_id","date","environment","period_type"}))
public class TrendSnapshot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long projectId;
    @Column(nullable = false) private LocalDate date;
    @Column(nullable = false) private String environment;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PeriodType periodType;
    private double passRate;
    private double failureRate;
    private double avgDurationMs;
    private double retryRate;
    public enum PeriodType { DAILY, WEEKLY }

    public TrendSnapshot() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getProjectId() { return projectId; } public void setProjectId(Long id) { this.projectId = id; }
    public LocalDate getDate() { return date; } public void setDate(LocalDate d) { this.date = d; }
    public String getEnvironment() { return environment; } public void setEnvironment(String e) { this.environment = e; }
    public PeriodType getPeriodType() { return periodType; } public void setPeriodType(PeriodType t) { this.periodType = t; }
    public double getPassRate() { return passRate; } public void setPassRate(double d) { this.passRate = d; }
    public double getFailureRate() { return failureRate; } public void setFailureRate(double d) { this.failureRate = d; }
    public double getAvgDurationMs() { return avgDurationMs; } public void setAvgDurationMs(double d) { this.avgDurationMs = d; }
    public double getRetryRate() { return retryRate; } public void setRetryRate(double d) { this.retryRate = d; }
}
