package com.pawhub.module.triage.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.common.exception.PawHubException;
import com.pawhub.module.auth.repository.MembershipRepository;
import com.pawhub.module.triage.dto.TriageRequest;
import com.pawhub.module.triage.dto.TriageResponse;
import com.pawhub.module.triage.dto.TriageSummaryResponse;
import com.pawhub.module.triage.service.TriageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
public class TriageController {
    private final TriageService triageService;
    private final MembershipRepository membershipRepo;

    public TriageController(TriageService s, MembershipRepository m) {
        this.triageService = s; this.membershipRepo = m;
    }

    @PutMapping("/test-executions/{executionId}/triage")
    public ApiResponse<TriageResponse> saveTriage(@PathVariable Long projectId,
            @PathVariable Long executionId, @Valid @RequestBody TriageRequest request,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        assertMember(projectId, userId);
        return ApiResponse.ok(triageService.saveOrUpdate(executionId, request, userId));
    }

    @GetMapping("/test-executions/{executionId}/triage")
    public ApiResponse<TriageResponse> getTriage(@PathVariable Long projectId,
            @PathVariable Long executionId) {
        TriageResponse triage = triageService.getByExecutionId(executionId);
        return ApiResponse.ok(triage);
    }

    @GetMapping("/triage-summary")
    public ApiResponse<TriageSummaryResponse> getSummary(@PathVariable Long projectId,
            @RequestParam(defaultValue = "30") int days) {
        return ApiResponse.ok(triageService.getSummary(projectId, days));
    }

    private void assertMember(Long projectId, Long userId) {
        boolean member = membershipRepo.existsByUserIdAndProjectTeamId(userId, projectId);
        if (!member) throw new PawHubException("Not a member of this project", HttpStatus.FORBIDDEN);
    }
}
