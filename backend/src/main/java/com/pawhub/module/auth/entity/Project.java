package com.pawhub.module.auth.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "projects")
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String name;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    @Column(nullable = false, unique = true)
    private String apiKey = "sk-" + UUID.randomUUID().toString().substring(0, 20);
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    public Project() {}
    public Project(String name, Team team) { this.name = name; this.team = team; }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public Team getTeam() { return team; } public void setTeam(Team t) { this.team = t; }
    public String getApiKey() { return apiKey; }
    public Instant getCreatedAt() { return createdAt; }
}
