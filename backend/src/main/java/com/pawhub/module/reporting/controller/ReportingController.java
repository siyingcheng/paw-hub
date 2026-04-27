package com.pawhub.module.reporting.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.module.reporting.dto.SummaryResponse;
import com.pawhub.module.reporting.service.ExportService;
import com.pawhub.module.reporting.service.SummaryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
public class ReportingController {
    private final SummaryService summaryService;
    private final ExportService exportService;

    public ReportingController(SummaryService s, ExportService e) {
        this.summaryService = s; this.exportService = e;
    }

    @GetMapping("/summary")
    public ApiResponse<SummaryResponse> getSummary(@PathVariable Long projectId,
            @RequestParam(defaultValue = "30") int days) {
        return ApiResponse.ok(summaryService.getSummary(projectId, days));
    }

    @GetMapping("/export")
    public ApiResponse<String> export(@PathVariable Long projectId,
            @RequestParam(defaultValue = "json") String format,
            @RequestParam(defaultValue = "30") int days) {
        if (!"json".equals(format)) return ApiResponse.error("Only JSON export supported in v1");
        return ApiResponse.ok(exportService.exportJsonSummary(projectId, days));
    }
}
