package com.pawhub.module.reporting.service;

import com.pawhub.module.collection.entity.TestExecution;
import com.pawhub.module.collection.repository.TestExecutionRepository;
import com.pawhub.module.reporting.dto.*;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SummaryService {
    private final TestExecutionRepository executionRepo;

    public SummaryService(TestExecutionRepository e) { this.executionRepo = e; }

    public SummaryResponse getSummary(Long projectId, int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        var failures = executionRepo.findRecentFailures(projectId, since);
        long totalFailures = failures.size();

        TopFailure topFailure = null;
        if (!failures.isEmpty()) {
            Map<String, Long> freq = failures.stream()
                .filter(e -> e.getErrorMessage() != null)
                .collect(Collectors.groupingBy(
                    e -> e.getSuiteName() + "." + e.getTestName(),
                    Collectors.counting()));
            var top = freq.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
            if (top != null) {
                String errMsg = failures.stream()
                    .filter(e -> (e.getSuiteName() + "." + e.getTestName()).equals(top.getKey()))
                    .findFirst().map(TestExecution::getErrorMessage).orElse("");
                topFailure = new TopFailure(top.getKey(), errMsg, top.getValue());
            }
        }
        return new SummaryResponse(0, 0, totalFailures, topFailure, List.of());
    }
}
