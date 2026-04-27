package com.pawhub.module.analysis.repository;
import com.pawhub.module.analysis.entity.TrendSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrendSnapshotRepository extends JpaRepository<TrendSnapshot, Long> {
    List<TrendSnapshot> findByProjectIdAndEnvironmentAndPeriodTypeAndDateBetweenOrderByDateAsc(
        Long projectId, String environment, TrendSnapshot.PeriodType periodType, LocalDate from, LocalDate to);
    Optional<TrendSnapshot> findByProjectIdAndDateAndEnvironmentAndPeriodType(
        Long projectId, LocalDate date, String environment, TrendSnapshot.PeriodType periodType);
}
