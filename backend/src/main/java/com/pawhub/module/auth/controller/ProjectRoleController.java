package com.pawhub.module.auth.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.common.exception.PawHubException;
import com.pawhub.module.auth.entity.Project;
import com.pawhub.module.auth.repository.MembershipRepository;
import com.pawhub.module.auth.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
public class ProjectRoleController {
    private final ProjectRepository projectRepo;
    private final MembershipRepository membershipRepo;

    public ProjectRoleController(ProjectRepository p, MembershipRepository m) {
        this.projectRepo = p; this.membershipRepo = m;
    }

    @GetMapping("/my-role")
    public ApiResponse<RoleResponse> getMyRole(@PathVariable Long projectId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> new PawHubException("Project not found", HttpStatus.NOT_FOUND));
        String role = membershipRepo.findByUserIdAndTeamId(userId, project.getTeam().getId())
            .map(m -> m.getRole().name())
            .orElse("NONE");
        return ApiResponse.ok(new RoleResponse(role));
    }

    public record RoleResponse(String role) {}
}
