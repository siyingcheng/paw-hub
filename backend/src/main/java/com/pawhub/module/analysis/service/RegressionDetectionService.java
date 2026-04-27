package com.pawhub.module.analysis.service;

import com.pawhub.module.analysis.entity.TrendSnapshot;
import com.pawhub.module.analysis.repository.TrendSnapshotRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class RegressionDetectionService {
    private final TrendSnapshotRepository trendRepo;

    public RegressionDetectionService(TrendSnapshotRepository tr) { this.trendRepo = tr; }

    public boolean detectRunLevelRegression(Long projectId, String env, TrendSnapshot today, double sigma) {
        LocalDate windowStart = today.getDate().minusDays(30);
        List<TrendSnapshot> history = trendRepo
            .findByProjectIdAndEnvironmentAndPeriodTypeAndDateBetweenOrderByDateAsc(
                projectId, env, TrendSnapshot.PeriodType.DAILY, windowStart, today.getDate().minusDays(1));
        if (history.size() < 5) return false;

        double mean = history.stream().mapToDouble(TrendSnapshot::getPassRate).average().orElse(0);
        double variance = history.stream().mapToDouble(s -> Math.pow(s.getPassRate() - mean, 2)).average().orElse(0);
        double stdDev = Math.sqrt(variance);
        return today.getPassRate() < (mean - sigma * stdDev);
    }
}
