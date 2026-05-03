package com.pawhub.module.auth.service;

import com.pawhub.common.exception.PawHubException;
import com.pawhub.module.auth.entity.*;
import com.pawhub.module.auth.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TenantService {
    private final TeamRepository teamRepo;
    private final ProjectRepository projectRepo;
    private final MembershipRepository membershipRepo;
    private final UserRepository userRepo;

    public TenantService(TeamRepository t, ProjectRepository p,
                         MembershipRepository m, UserRepository u) {
        this.teamRepo = t; this.projectRepo = p;
        this.membershipRepo = m; this.userRepo = u;
    }

    public Team createTeam(String name) {
        return teamRepo.save(new Team(name));
    }

    @Transactional
    public Project createProject(Long teamId, String name, Long userId) {
        Team team = teamRepo.findById(teamId)
            .orElseThrow(() -> new PawHubException("Team not found", HttpStatus.NOT_FOUND));
        if (!membershipRepo.existsByUserIdAndTeamId(userId, teamId))
            throw new PawHubException("Not a team member", HttpStatus.FORBIDDEN);
        return projectRepo.save(new Project(name, team));
    }

    @Transactional
    public Membership addMember(Long teamId, Long userId, MembershipRole role) {
        if (membershipRepo.existsByUserIdAndTeamId(userId, teamId))
            throw new PawHubException("Already a member", HttpStatus.CONFLICT);
        User u = userRepo.findById(userId)
            .orElseThrow(() -> new PawHubException("User not found", HttpStatus.NOT_FOUND));
        Team t = teamRepo.findById(teamId)
            .orElseThrow(() -> new PawHubException("Team not found", HttpStatus.NOT_FOUND));
        return membershipRepo.save(new Membership(u, t, role));
    }

    // ── Role management ──

    @Transactional(readOnly = true)
    public List<Membership> getMembers(Long teamId, Long actorId) {
        requireAdmin(teamId, actorId);
        return membershipRepo.findByTeamIdWithUser(teamId);
    }

    @Transactional
    public Membership upsertRole(Long teamId, Long userId, Long actorId, MembershipRole role) {
        requireAdmin(teamId, actorId);
        if (userId.equals(actorId))
            throw new PawHubException("Cannot change your own role", HttpStatus.BAD_REQUEST);

        if (role != MembershipRole.ADMIN) {
            assertNotLastAdmin(teamId, userId);
        }

        Membership m = membershipRepo.findByUserIdAndTeamIdWithUser(userId, teamId)
            .orElseThrow(() -> new PawHubException("User is not a member of this team", HttpStatus.NOT_FOUND));
        m.setRole(role);
        return membershipRepo.save(m);
    }

    @Transactional
    public void removeMember(Long teamId, Long userId, Long actorId) {
        requireAdmin(teamId, actorId);
        if (userId.equals(actorId))
            throw new PawHubException("Cannot remove yourself from the team", HttpStatus.BAD_REQUEST);
        assertNotLastAdmin(teamId, userId);

        Membership m = membershipRepo.findByUserIdAndTeamId(userId, teamId)
            .orElseThrow(() -> new PawHubException("User is not a member of this team", HttpStatus.NOT_FOUND));
        membershipRepo.delete(m);
    }

    // ── Guard helpers ──

    private void requireAdmin(Long teamId, Long userId) {
        Membership m = membershipRepo.findByUserIdAndTeamId(userId, teamId)
            .orElseThrow(() -> new PawHubException("Not a team member", HttpStatus.FORBIDDEN));
        if (m.getRole() != MembershipRole.ADMIN)
            throw new PawHubException("Only admins can manage team members", HttpStatus.FORBIDDEN);
    }

    private void assertNotLastAdmin(Long teamId, Long userId) {
        Membership target = membershipRepo.findByUserIdAndTeamId(userId, teamId).orElse(null);
        if (target == null || target.getRole() != MembershipRole.ADMIN) return;
        long adminCount = membershipRepo.countByTeamIdAndRole(teamId, MembershipRole.ADMIN);
        if (adminCount <= 1)
            throw new PawHubException("Cannot remove the last admin of the team", HttpStatus.BAD_REQUEST);
    }
}
