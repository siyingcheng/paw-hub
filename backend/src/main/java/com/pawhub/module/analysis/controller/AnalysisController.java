package com.pawhub.module.analysis.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.module.analysis.dto.*;
import com.pawhub.module.analysis.entity.TrendSnapshot;
import com.pawhub.module.analysis.repository.*;
import com.pawhub.module.analysis.service.RegressionDetectionService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
public class AnalysisController {
    private final TrendSnapshotRepository trendRepo;
    private final FlakyTestRecordRepository flakyRepo;
    private final FailureClusterRepository clusterRepo;
    private final RegressionDetectionService regressionService;

    public AnalysisController(TrendSnapshotRepository t, FlakyTestRecordRepository f,
                              FailureClusterRepository c, RegressionDetectionService r) {
        this.trendRepo = t; this.flakyRepo = f; this.clusterRepo = c;
        this.regressionService = r;
    }

    @GetMapping("/trends")
    public ApiResponse<List<TrendResponse>> getTrends(@PathVariable Long projectId,
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(required = false) String environment) {
        TrendSnapshot.PeriodType periodType;
        try {
            periodType = TrendSnapshot.PeriodType.valueOf(period.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("Invalid period type. Use 'daily' or 'weekly'");
        }
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

    @GetMapping("/regressions")
    public ApiResponse<List<RegressionResponse>> getRegressions(@PathVariable Long projectId) {
        var results = regressionService.detectAll(projectId);
        var list = results.stream()
            .filter(r -> r.runLevelRegression() || !r.regressedCases().isEmpty())
            .map(r -> new RegressionResponse(
                r.runLevelRegression(),
                r.runLevelDetail(),
                r.regressedCases())).toList();
        return ApiResponse.ok(list);
    }
}
