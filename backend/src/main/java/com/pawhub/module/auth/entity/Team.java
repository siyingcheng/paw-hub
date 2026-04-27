package com.pawhub.module.auth.entity;
import jakarta.persistence.*;

@Entity @Table(name = "teams")
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String name;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;
    public Team() {}
    public Team(String name, Organization org) { this.name = name; this.organization = org; }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization org) { this.organization = org; }
}
