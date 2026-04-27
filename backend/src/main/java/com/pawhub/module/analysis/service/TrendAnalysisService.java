package com.pawhub.module.analysis.service;

import com.pawhub.module.analysis.entity.TrendSnapshot;
import com.pawhub.module.analysis.repository.TrendSnapshotRepository;
import com.pawhub.module.collection.entity.TestRun;
import com.pawhub.module.collection.repository.TestRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
public class TrendAnalysisService {
    private final TestRunRepository testRunRepo;
    private final TrendSnapshotRepository trendRepo;

    public TrendAnalysisService(TestRunRepository t, TrendSnapshotRepository tr) {
        this.testRunRepo = t; this.trendRepo = tr;
    }

    @Transactional
    public void computeDailyTrend(Long projectId, String env, LocalDate date) {
        Instant from = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        List<TestRun> runs = testRunRepo.findByProjectIdAndEnvironmentAndCreatedAtBetween(projectId, env, from, to);
        if (runs.isEmpty()) return;

        TrendSnapshot snap = trendRepo
            .findByProjectIdAndDateAndEnvironmentAndPeriodType(projectId, date, env, TrendSnapshot.PeriodType.DAILY)
            .orElseGet(TrendSnapshot::new);
        snap.setProjectId(projectId);
        snap.setDate(date);
        snap.setEnvironment(env);
        snap.setPeriodType(TrendSnapshot.PeriodType.DAILY);
        snap.setPassRate(runs.stream().mapToDouble(r -> r.getTotalCases() > 0 ? (double)r.getPassed()/r.getTotalCases() : 1.0).average().orElse(0));
        snap.setFailureRate(runs.stream().mapToDouble(r -> r.getTotalCases() > 0 ? (double)r.getFailed()/r.getTotalCases() : 0.0).average().orElse(0));
        snap.setAvgDurationMs(runs.stream().mapToDouble(TestRun::getDurationMs).average().orElse(0));
        snap.setRetryRate(0.0);
        trendRepo.save(snap);
    }
}
