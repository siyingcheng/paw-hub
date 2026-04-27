package com.pawhub.module.collection.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.module.collection.dto.TestExecutionResponse;
import com.pawhub.module.collection.dto.TestRunDetailResponse;
import com.pawhub.module.collection.dto.TestRunResponse;
import com.pawhub.module.collection.entity.TestRun;
import com.pawhub.module.collection.repository.TestRunRepository;
import com.pawhub.module.collection.service.CollectionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
public class CollectionController {
    private final CollectionService service;
    private final TestRunRepository testRunRepo;
    public CollectionController(CollectionService s, TestRunRepository tr) {
        this.service = s; this.testRunRepo = tr;
    }

    @PostMapping(value = "/test-results", consumes = "multipart/form-data")
    public ApiResponse<TestRunResponse> uploadFile(@PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            @RequestParam String environment,
            @RequestParam(required = false) String runIdentifier,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String commitSha,
            @RequestParam(required = false) String triggeredBy) {
        try {
            String xml = new String(file.getBytes(), StandardCharsets.UTF_8);
            return ApiResponse.ok(TestRunResponse.from(
                service.ingest(projectId, environment, runIdentifier, branch, commitSha, triggeredBy, xml)));
        } catch (Exception e) {
            return ApiResponse.error("Failed to process: " + e.getMessage());
        }
    }

    @PostMapping(value = "/test-results", consumes = "application/json")
    public ApiResponse<TestRunResponse> uploadJson(@PathVariable Long projectId,
            @RequestBody String xmlBody,
            @RequestParam String environment,
            @RequestParam(required = false) String runIdentifier,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String commitSha,
            @RequestParam(required = false) String triggeredBy) {
        return ApiResponse.ok(TestRunResponse.from(
            service.ingest(projectId, environment, runIdentifier, branch, commitSha, triggeredBy, xmlBody)));
    }

    @GetMapping("/test-runs")
    public ApiResponse<List<TestRunResponse>> getRuns(@PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var runs = testRunRepo.findByProjectId(projectId, pageable)
            .map(TestRunResponse::from).toList();
        return ApiResponse.ok(runs);
    }

    @GetMapping("/test-runs/{runId}")
    public ApiResponse<TestRunDetailResponse> getRun(@PathVariable Long projectId,
                                                      @PathVariable Long runId) {
        TestRun run = service.getRun(projectId, runId);
        var execs = service.getExecutions(runId).stream()
            .map(TestExecutionResponse::from).toList();
        return ApiResponse.ok(TestRunDetailResponse.from(run, execs));
    }
}
