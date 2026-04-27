package com.pawhub.module.analysis.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.module.analysis.dto.*;
import com.pawhub.module.analysis.entity.TrendSnapshot;
import com.pawhub.module.analysis.repository.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
public class AnalysisController {
    private final TrendSnapshotRepository trendRepo;
    private final FlakyTestRecordRepository flakyRepo;
    private final FailureClusterRepository clusterRepo;

    public AnalysisController(TrendSnapshotRepository t, FlakyTestRecordRepository f,
                              FailureClusterRepository c) {
        this.trendRepo = t; this.flakyRepo = f; this.clusterRepo = c;
    }

    @GetMapping("/trends")
    public ApiResponse<List<TrendResponse>> getTrends(@PathVariable Long projectId,
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(required = false) String environment) {
        TrendSnapshot.PeriodType periodType = TrendSnapshot.PeriodType.valueOf(period.toUpperCase());
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days);
        String[] envs = environment != null ? new String[]{environment} : new String[]{"dev","staging","prod"};
        var responses = new ArrayList<TrendResponse>();
        for (String env : envs) {
            var snaps = trendRepo.findByProjectIdAndEnvironmentAndPeriodTypeAndDateBetweenOrderByDateAsc(
                projectId, env, periodType, from, to);
            var points = snaps.stream().map(s -> new TrendDataPoint(s.getDate(), s.getPassRate(),
                s.getFailureRate(), s.getAvgDurationMs(), s.getRetryRate())).toList();
            responses.add(new TrendResponse(env, period, points));
        }
        return ApiResponse.ok(responses);
    }

    @GetMapping("/flaky-tests")
    public ApiResponse<List<FlakyTestResponse>> getFlakyTests(@PathVariable Long projectId) {
        var list = flakyRepo.findByProjectIdOrderByFlakyScoreDesc(projectId)
            .stream().map(f -> new FlakyTestResponse(f.getTestCaseKey(), f.getFlakyScore(),
                f.getTransitionCount(), f.getRetryPassCount(), f.getLastDetectedAt())).toList();
        return ApiResponse.ok(list);
    }

    @GetMapping("/failure-clusters")
    public ApiResponse<List<FailureClusterResponse>> getClusters(@PathVariable Long projectId) {
        var list = clusterRepo.findByProjectIdOrderByOccurrenceCountDesc(projectId)
            .stream().map(c -> new FailureClusterResponse(c.getClusterKey(), c.getRepresentativeError(),
                c.getOccurrenceCount(), c.getFirstSeen(), c.getLastSeen())).toList();
        return ApiResponse.ok(list);
    }
}
