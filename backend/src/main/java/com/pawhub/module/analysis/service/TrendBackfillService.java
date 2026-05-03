package com.pawhub.module.analysis.service;

import com.pawhub.module.auth.repository.ProjectRepository;
import com.pawhub.module.collection.repository.TestRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrendBackfillService {
    private static final Logger log = LoggerFactory.getLogger(TrendBackfillService.class);
    private final ProjectRepository projectRepo;
    private final TestRunRepository testRunRepo;
    private final TrendAnalysisService trendService;

    public TrendBackfillService(ProjectRepository pr, TestRunRepository tr, TrendAnalysisService ts) {
        this.projectRepo = pr; this.testRunRepo = tr; this.trendService = ts;
    }

    @Scheduled(cron = "0 0 3 * * *") // 3 AM daily
    @Transactional
    public void backfillMissingDays() {
        log.info("Starting trend backfill");
        var projects = projectRepo.findAll();
        for (var project : projects) {
            List<String> envs = testRunRepo.findDistinctEnvironments(project.getId());
            LocalDate today = LocalDate.now();
            LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
            for (String env : envs) {
                for (int d = 0; d <= 30; d++) {
                    LocalDate date = today.minusDays(d);
                    trendService.computeDailyTrend(project.getId(), env, date);
                }
                for (int w = 0; w <= 12; w++) {
                    LocalDate ws = weekStart.minusWeeks(w);
                    trendService.computeWeeklyTrend(project.getId(), env, ws);
                }
            }
        }
        log.info("Trend backfill complete");
    }
}
