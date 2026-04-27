package com.pawhub.module.auth.entity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "organizations")
public class Organization {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    public Organization() {}
    public Organization(String name) { this.name = name; }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public Instant getCreatedAt() { return createdAt; }
}
