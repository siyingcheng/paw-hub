package com.pawhub.module.auth.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.common.exception.PawHubException;
import com.pawhub.module.auth.dto.TeamMemberResponse;
import com.pawhub.module.auth.dto.UpsertRoleRequest;
import com.pawhub.module.auth.entity.Project;
import com.pawhub.module.auth.repository.ProjectRepository;
import com.pawhub.module.auth.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/team/members")
public class TeamMemberController {

    private final TenantService tenantService;
    private final ProjectRepository projectRepo;

    public TeamMemberController(TenantService ts, ProjectRepository pr) {
        this.tenantService = ts;
        this.projectRepo = pr;
    }

    @GetMapping
    public ApiResponse<List<TeamMemberResponse>> list(@PathVariable Long projectId, Authentication auth) {
        Long actorId = (Long) auth.getPrincipal();
        Long teamId = getTeamId(projectId);
        var members = tenantService.getMembers(teamId, actorId).stream()
            .map(m -> new TeamMemberResponse(
                m.getUser().getId(), m.getUser().getUsername(),
                m.getUser().getEmail(), m.getRole().name()))
            .toList();
        return ApiResponse.ok(members);
    }

    @PutMapping("/{userId}")
    public ApiResponse<TeamMemberResponse> upsertRole(@PathVariable Long projectId,
                                                       @PathVariable Long userId,
                                                       @Valid @RequestBody UpsertRoleRequest body,
                                                       Authentication auth) {
        Long actorId = (Long) auth.getPrincipal();
        Long teamId = getTeamId(projectId);
        var m = tenantService.upsertRole(teamId, userId, actorId, body.role());
        return ApiResponse.ok(new TeamMemberResponse(
            m.getUser().getId(), m.getUser().getUsername(),
            m.getUser().getEmail(), m.getRole().name()));
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<Void> remove(@PathVariable Long projectId,
                                    @PathVariable Long userId,
                                    Authentication auth) {
        Long actorId = (Long) auth.getPrincipal();
        Long teamId = getTeamId(projectId);
        tenantService.removeMember(teamId, userId, actorId);
        return ApiResponse.ok();
    }

    private Long getTeamId(Long projectId) {
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> new PawHubException("Project not found", HttpStatus.NOT_FOUND));
        return project.getTeam().getId();
    }
}
