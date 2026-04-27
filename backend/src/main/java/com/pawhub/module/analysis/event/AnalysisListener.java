package com.pawhub.module.analysis.event;

import com.pawhub.module.analysis.service.*;
import com.pawhub.module.collection.event.TestResultCollectedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class AnalysisListener {
    private final TrendAnalysisService trendService;
    private final FlakyDetectionService flakyService;
    private final FailureClusteringService clusterService;

    public AnalysisListener(TrendAnalysisService t, FlakyDetectionService f, FailureClusteringService c) {
        this.trendService = t; this.flakyService = f; this.clusterService = c;
    }

    @Async("analysisExecutor")
    @EventListener
    public void onTestResultCollected(TestResultCollectedEvent event) {
        String[] envs = {"dev", "staging", "prod"};
        LocalDate today = LocalDate.now();
        for (String env : envs) {
            trendService.computeDailyTrend(event.projectId(), env, today);
        }
        flakyService.detectFlakyTests(event.projectId());
        clusterService.clusterFailures(event.projectId());
    }
}
