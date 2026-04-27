package com.pawhub.module.triage.repository;

import com.pawhub.module.triage.entity.FailureTriage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FailureTriageRepository extends JpaRepository<FailureTriage, Long> {
    Optional<FailureTriage> findByTestExecutionId(Long executionId);

    @Query("SELECT ft.triageStatus, COUNT(ft) FROM FailureTriage ft " +
           "JOIN ft.testExecution te JOIN te.testRun tr " +
           "WHERE tr.project.id = :projectId AND tr.createdAt >= :since " +
           "GROUP BY ft.triageStatus")
    List<Object[]> countByStatus(@Param("projectId") Long projectId, @Param("since") java.time.Instant since);
}
