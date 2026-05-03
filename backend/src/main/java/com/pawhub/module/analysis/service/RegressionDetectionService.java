package com.pawhub.module.analysis.service;

import com.pawhub.module.analysis.entity.TrendSnapshot;
import com.pawhub.module.analysis.repository.TrendSnapshotRepository;
import com.pawhub.module.collection.entity.TestExecution;
import com.pawhub.module.collection.entity.TestStatus;
import com.pawhub.module.collection.repository.TestExecutionRepository;
import com.pawhub.module.collection.repository.TestRunRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class RegressionDetectionService {
    private final TrendSnapshotRepository trendRepo;
    private final TestExecutionRepository executionRepo;
    private final TestRunRepository testRunRepo;
    private final double sigma;
    private final int consecutivePassCount;

    public RegressionDetectionService(TrendSnapshotRepository tr, TestExecutionRepository er,
                                      TestRunRepository rr,
                                      @Value("${analysis.regression-sigma}") double sigma,
                                      @Value("${analysis.consecutive-pass-count}") int consecutivePassCount) {
        this.trendRepo = tr;
        this.executionRepo = er;
        this.testRunRepo = rr;
        this.sigma = sigma;
        this.consecutivePassCount = consecutivePassCount;
    }

    public record RegressionResult(boolean runLevelRegression, String runLevelDetail,
                                   List<String> regressedCases) {}

    public List<RegressionResult> detectAll(Long projectId) {
        List<RegressionResult> results = new ArrayList<>();
        List<String> envs = testRunRepo.findDistinctEnvironments(projectId);
        LocalDate today = LocalDate.now();
        for (String env : envs) {
            var snap = trendRepo.findByProjectIdAndDateAndEnvironmentAndPeriodType(
                projectId, today, env, TrendSnapshot.PeriodType.DAILY).orElse(null);
            if (snap != null) {
                results.add(detect(projectId, env, snap));
            }
        }
        return results;
    }

    public RegressionResult detect(Long projectId, String env, TrendSnapshot today) {
        boolean runLevel = detectRunLevel(projectId, env, today, sigma);
        String runDetail = null;
        if (runLevel) {
            double mean = getRollingMean(projectId, env, today.getDate());
            runDetail = String.format("Pass rate %.1f%% below rolling avg %.1f%%",
                today.getPassRate() * 100, mean * 100);
        }
        List<String> cases = detectCaseLevel(projectId, today.getDate());
        return new RegressionResult(runLevel, runDetail, cases);
    }

    private boolean detectRunLevel(Long projectId, String env, TrendSnapshot today, double sigma) {
        LocalDate windowStart = today.getDate().minusDays(30);
        List<TrendSnapshot> history = trendRepo
            .findByProjectIdAndEnvironmentAndPeriodTypeAndDateBetweenOrderByDateAsc(
                projectId, env, TrendSnapshot.PeriodType.DAILY, windowStart, today.getDate().minusDays(1));
        if (history.size() < 5) return false;
        double mean = getRollingMean(projectId, env, today.getDate());
        double variance = history.stream().mapToDouble(s -> Math.pow(s.getPassRate() - mean, 2)).average().orElse(0);
        double stdDev = Math.sqrt(variance);
        if (stdDev < 0.005) return false; // too stable to be a meaningful regression
        return today.getPassRate() < (mean - sigma * stdDev);
    }

    private double getRollingMean(Long projectId, String env, LocalDate date) {
        LocalDate windowStart = date.minusDays(30);
        List<TrendSnapshot> history = trendRepo
            .findByProjectIdAndEnvironmentAndPeriodTypeAndDateBetweenOrderByDateAsc(
                projectId, env, TrendSnapshot.PeriodType.DAILY, windowStart, date.minusDays(1));
        return history.stream().mapToDouble(TrendSnapshot::getPassRate).average().orElse(1.0);
    }

    // Case-level: tests that passed N consecutive runs and now failed
    private List<String> detectCaseLevel(Long projectId, LocalDate date) {
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        List<String> allKeys = executionRepo.findDistinctTestCaseKeys(projectId, since);
        List<String> regressed = new ArrayList<>();

        for (String key : allKeys) {
            List<TestExecution> execs = executionRepo.findByTestCaseKey(projectId, key, since);
            if (execs.size() < consecutivePassCount + 1) continue;

            // Check latest execution (from most recent run)
            TestExecution latest = execs.get(0); // ordered by createdAt DESC
            if (latest.getStatus() != TestStatus.FAIL && latest.getStatus() != TestStatus.ERROR) continue;

            // Count consecutive passes before the latest failure
            int consecutivePasses = 0;
            for (int i = 1; i < execs.size(); i++) {
                TestExecution prev = execs.get(i);
                if (prev.getStatus() == TestStatus.PASS) {
                    consecutivePasses++;
                } else {
                    break;
                }
            }
            if (consecutivePasses >= consecutivePassCount) {
                regressed.add(key);
            }
        }
        return regressed;
    }
}
