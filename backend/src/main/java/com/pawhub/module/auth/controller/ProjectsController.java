package com.pawhub.module.auth.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.module.auth.entity.Project;
import com.pawhub.module.auth.repository.MembershipRepository;
import com.pawhub.module.auth.repository.ProjectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectsController {
    private final ProjectRepository projectRepo;
    private final MembershipRepository membershipRepo;

    public ProjectsController(ProjectRepository pr, MembershipRepository mr) {
        this.projectRepo = pr; this.membershipRepo = mr;
    }

    @GetMapping
    public ApiResponse<List<ProjectItem>> list(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        List<Long> teamIds = membershipRepo.findByUserId(userId).stream()
            .map(m -> m.getTeam().getId())
            .distinct()
            .toList();
        if (teamIds.isEmpty()) return ApiResponse.ok(List.of());
        List<Project> projects = projectRepo.findByTeamIdInWithTeam(teamIds);
        List<ProjectItem> items = projects.stream()
            .map(p -> new ProjectItem(p.getId(), p.getName(), p.getTeam().getName()))
            .toList();
        return ApiResponse.ok(items);
    }

    public record ProjectItem(Long id, String name, String teamName) {}
}
