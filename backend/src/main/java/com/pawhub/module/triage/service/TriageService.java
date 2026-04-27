package com.pawhub.module.triage.service;

import com.pawhub.common.exception.PawHubException;
import com.pawhub.module.collection.repository.TestExecutionRepository;
import com.pawhub.module.triage.dto.TriageRequest;
import com.pawhub.module.triage.dto.TriageResponse;
import com.pawhub.module.triage.dto.TriageSummaryResponse;
import com.pawhub.module.triage.entity.FailureTriage;
import com.pawhub.module.triage.repository.FailureTriageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class TriageService {
    private final FailureTriageRepository triageRepo;
    private final TestExecutionRepository executionRepo;

    public TriageService(FailureTriageRepository t, TestExecutionRepository e) {
        this.triageRepo = t; this.executionRepo = e;
    }

    @Transactional
    public TriageResponse saveOrUpdate(Long executionId, TriageRequest request, Long userId) {
        executionRepo.findById(executionId)
            .orElseThrow(() -> new PawHubException("Test execution not found", HttpStatus.NOT_FOUND));
        FailureTriage triage = triageRepo.findByTestExecutionId(executionId)
            .orElseGet(FailureTriage::new);
        triage.setTestExecution(executionRepo.getReferenceById(executionId));
        triage.setTriageStatus(request.triageStatus());
        triage.setIssueLink(request.issueLink());
        triage.setComment(request.comment());
        triage.setAnnotatedBy(userId);
        triage.setUpdatedAt(Instant.now());
        triage = triageRepo.save(triage);
        return TriageResponse.from(triage);
    }

    public TriageResponse getByExecutionId(Long executionId) {
        return triageRepo.findByTestExecutionId(executionId)
            .map(TriageResponse::from)
            .orElse(null);
    }

    public TriageSummaryResponse getSummary(Long projectId) {
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Object[]> counts = triageRepo.countByStatus(projectId, since);
        var breakdown = new HashMap<FailureTriage.TriageStatus, Long>();
        long total = 0;
        for (Object[] row : counts) {
            FailureTriage.TriageStatus status = (FailureTriage.TriageStatus) row[0];
            long count = (Long) row[1];
            breakdown.put(status, count);
            total += count;
        }
        long untriaged = breakdown.getOrDefault(FailureTriage.TriageStatus.UNTRIAGED, 0L);
        return new TriageSummaryResponse(total, untriaged, breakdown);
    }
}
