package com.pawhub.module.analysis.service;

import com.pawhub.module.analysis.entity.FlakyTestRecord;
import com.pawhub.module.analysis.repository.FlakyTestRecordRepository;
import com.pawhub.module.collection.entity.TestExecution;
import com.pawhub.module.collection.entity.TestStatus;
import com.pawhub.module.collection.repository.TestExecutionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class FlakyDetectionService {
    private final TestExecutionRepository executionRepo;
    private final FlakyTestRecordRepository flakyRepo;
    private final double threshold;
    private final int windowDays;

    public FlakyDetectionService(TestExecutionRepository e, FlakyTestRecordRepository f,
                                 @Value("${analysis.flaky-threshold}") double threshold,
                                 @Value("${analysis.window-days}") int windowDays) {
        this.executionRepo = e; this.flakyRepo = f; this.threshold = threshold;
        this.windowDays = windowDays;
    }

    @Transactional
    public void detectFlakyTests(Long projectId) {
        Instant since = Instant.now().minus(windowDays, ChronoUnit.DAYS);
        List<String> keys = executionRepo.findDistinctTestCaseKeys(projectId, since);

        for (String key : keys) {
            List<TestExecution> execs = executionRepo.findByTestCaseKey(projectId, key, since);
            if (execs.size() < 3) continue;

            int transitions = 0;
            int retryPass = 0;
            TestStatus prev = null;
            for (TestExecution e : execs) {
                if (prev != null && e.getStatus() != prev) transitions++;
                if (e.getAttempt() > 1 && e.getStatus() == TestStatus.PASS) retryPass++;
                prev = e.getStatus();
            }
            double score = (double) transitions / execs.size() + (double) retryPass / execs.size() * 0.3;
            score = Math.min(1.0, score);

            if (score >= threshold) {
                FlakyTestRecord rec = flakyRepo.findByProjectIdAndTestCaseKey(projectId, key)
                    .orElseGet(FlakyTestRecord::new);
                rec.setProjectId(projectId);
                rec.setTestCaseKey(key);
                rec.setFlakyScore(score);
                rec.setTransitionCount(transitions);
                rec.setRetryPassCount(retryPass);
                rec.setLastDetectedAt(Instant.now());
                flakyRepo.save(rec);
            }
        }
    }
}
