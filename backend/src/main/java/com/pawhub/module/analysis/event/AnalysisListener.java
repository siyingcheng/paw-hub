package com.pawhub.module.analysis.event;

import com.pawhub.module.analysis.service.*;
import com.pawhub.module.collection.event.TestResultCollectedEvent;
import com.pawhub.module.collection.repository.TestRunRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
public class AnalysisListener {
    private final TrendAnalysisService trendService;
    private final RegressionDetectionService regressionService;
    private final FlakyDetectionService flakyService;
    private final FailureClusteringService clusterService;
    private final TestRunRepository testRunRepo;

    public AnalysisListener(TrendAnalysisService t, RegressionDetectionService r,
                           FlakyDetectionService f, FailureClusteringService c,
                           TestRunRepository tr) {
        this.trendService = t;
        this.regressionService = r;
        this.flakyService = f;
        this.clusterService = c;
        this.testRunRepo = tr;
    }

    @Async("analysisExecutor")
    @EventListener
    public void onTestResultCollected(TestResultCollectedEvent event) {
        List<String> envs = testRunRepo.findDistinctEnvironments(event.projectId());
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        for (String env : envs) {
            trendService.computeDailyTrend(event.projectId(), env, today);
            trendService.computeWeeklyTrend(event.projectId(), env, weekStart);
        }
        regressionService.detectAll(event.projectId());
        flakyService.detectFlakyTests(event.projectId());
        clusterService.clusterFailures(event.projectId());
    }
}
