package com.pawhub.module.auth.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.common.exception.PawHubException;
import com.pawhub.config.AnalysisProperties;
import com.pawhub.module.auth.entity.Project;
import com.pawhub.module.auth.repository.MembershipRepository;
import com.pawhub.module.auth.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
public class ProjectRoleController {
    private final ProjectRepository projectRepo;
    private final MembershipRepository membershipRepo;
    private final AnalysisProperties analysisProps;

    public ProjectRoleController(ProjectRepository p, MembershipRepository m, AnalysisProperties ap) {
        this.projectRepo = p; this.membershipRepo = m; this.analysisProps = ap;
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

    @GetMapping
    @Transactional(readOnly = true)
    public ApiResponse<ProjectResponse> getProject(@PathVariable Long projectId) {
        var project = projectRepo.findByIdWithTeam(projectId)
            .orElseThrow(() -> new PawHubException("Project not found", HttpStatus.NOT_FOUND));
        return ApiResponse.ok(new ProjectResponse(
            project.getId(), project.getName(), project.getApiKey(),
            project.getTeam().getName()));
    }

    @GetMapping("/analysis-config")
    public ApiResponse<AnalysisConfigResponse> getAnalysisConfig() {
        return ApiResponse.ok(new AnalysisConfigResponse(
            analysisProps.getFlakyThreshold(),
            analysisProps.getRegressionSigma(),
            analysisProps.getWindowDays()));
    }

    public record RoleResponse(String role) {}
    public record ProjectResponse(Long id, String name, String apiKey, String teamName) {}
    public record AnalysisConfigResponse(double flakyThreshold, double regressionSigma, int windowDays) {}
}
