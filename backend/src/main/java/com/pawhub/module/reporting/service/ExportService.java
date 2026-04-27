package com.pawhub.module.reporting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class ExportService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SummaryService summaryService;

    public ExportService(SummaryService s) { this.summaryService = s; }

    public String exportJsonSummary(Long projectId, int days) {
        try {
            var summary = summaryService.getSummary(projectId, days);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(summary);
        } catch (Exception e) { throw new RuntimeException("Export failed", e); }
    }
}
