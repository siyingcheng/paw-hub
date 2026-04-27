package com.pawhub.module.analysis.service;

import com.pawhub.module.analysis.entity.FailureCluster;
import com.pawhub.module.analysis.repository.FailureClusterRepository;
import com.pawhub.module.collection.entity.TestExecution;
import com.pawhub.module.collection.repository.TestExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FailureClusteringService {
    private final TestExecutionRepository executionRepo;
    private final FailureClusterRepository clusterRepo;

    public FailureClusteringService(TestExecutionRepository e, FailureClusterRepository c) {
        this.executionRepo = e; this.clusterRepo = c;
    }

    @Transactional
    public void clusterFailures(Long projectId) {
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        List<TestExecution> failures = executionRepo.findRecentFailures(projectId, since);
        if (failures.isEmpty()) return;

        Map<String, List<TestExecution>> groups = failures.stream()
            .filter(e -> e.getErrorMessage() != null && !e.getErrorMessage().isEmpty())
            .collect(Collectors.groupingBy(e -> hash(normalize(e.getErrorMessage()))));

        for (var entry : groups.entrySet()) {
            FailureCluster cluster = clusterRepo.findByProjectIdAndClusterKey(projectId, entry.getKey())
                .orElseGet(FailureCluster::new);
            cluster.setProjectId(projectId);
            cluster.setClusterKey(entry.getKey());
            String rep = entry.getValue().stream()
                .map(TestExecution::getErrorMessage)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("");
            cluster.setRepresentativeError(rep.substring(0, Math.min(rep.length(), 1000)));
            cluster.setOccurrenceCount(entry.getValue().size());
            Instant now = Instant.now();
            if (cluster.getFirstSeen() == null) cluster.setFirstSeen(now);
            cluster.setLastSeen(now);
            clusterRepo.save(cluster);
        }
    }

    private String normalize(String msg) {
        return msg.replaceAll("\\d+", "0")
                .replaceAll("0x[0-9a-fA-F]+", "0xHEX")
                .replaceAll("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", "UUID");
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes()));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
