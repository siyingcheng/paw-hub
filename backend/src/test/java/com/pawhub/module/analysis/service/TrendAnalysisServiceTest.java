package com.pawhub.module.analysis.service;

import com.pawhub.module.analysis.entity.TrendSnapshot;
import com.pawhub.module.analysis.repository.TrendSnapshotRepository;
import com.pawhub.module.auth.entity.Project;
import com.pawhub.module.collection.entity.TestRun;
import com.pawhub.module.collection.repository.TestRunRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrendAnalysisServiceTest {
    @Mock TestRunRepository testRunRepo;
    @Mock TrendSnapshotRepository trendRepo;
    @InjectMocks TrendAnalysisService service;

    @Test
    void shouldComputeDailyTrend() {
        TestRun run = new TestRun();
        run.setProject(project(1L));
        run.setTotalCases(10); run.setPassed(9); run.setFailed(1);
        run.setDurationMs(5000); run.setEnvironment("prod");

        when(testRunRepo.findByProjectIdAndEnvironmentAndCreatedAtBetween(any(), any(), any(), any()))
            .thenReturn(List.of(run));
        when(trendRepo.findByProjectIdAndDateAndEnvironmentAndPeriodType(any(), any(), any(), any()))
            .thenReturn(Optional.empty());

        service.computeDailyTrend(1L, "prod", LocalDate.of(2026, 4, 27));

        ArgumentCaptor<TrendSnapshot> cap = ArgumentCaptor.forClass(TrendSnapshot.class);
        verify(trendRepo).save(cap.capture());
        assertThat(cap.getValue().getPassRate()).isEqualTo(0.9);
    }

    @Test
    void shouldSkipWhenNoRuns() {
        when(testRunRepo.findByProjectIdAndEnvironmentAndCreatedAtBetween(any(), any(), any(), any()))
            .thenReturn(List.of());

        service.computeDailyTrend(1L, "prod", LocalDate.now());
        verify(trendRepo, never()).save(any());
    }

    private Project project(Long id) {
        Project p = new Project();
        p.setId(id); return p;
    }
}
