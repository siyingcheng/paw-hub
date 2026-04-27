package com.pawhub.module.auth.entity;
import jakarta.persistence.*;

@Entity @Table(name = "memberships", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "team_id"}))
public class Membership {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private MembershipRole role;
    public Membership() {}
    public Membership(User u, Team t, MembershipRole r) { this.user = u; this.team = t; this.role = r; }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public User getUser() { return user; } public void setUser(User u) { this.user = u; }
    public Team getTeam() { return team; } public void setTeam(Team t) { this.team = t; }
    public MembershipRole getRole() { return role; } public void setRole(MembershipRole r) { this.role = r; }
}
