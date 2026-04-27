package com.pawhub.module.collection.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.module.collection.dto.TestRunResponse;
import com.pawhub.module.collection.service.CollectionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
public class CollectionController {
    private final CollectionService service;
    public CollectionController(CollectionService s) { this.service = s; }

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
}
