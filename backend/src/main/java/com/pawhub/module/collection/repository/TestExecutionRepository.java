package com.pawhub.module.collection.repository;

import com.pawhub.module.collection.entity.TestExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface TestExecutionRepository extends JpaRepository<TestExecution, Long> {
    List<TestExecution> findByTestRunIdOrderByAttemptAsc(Long testRunId);

    @Query("SELECT te FROM TestExecution te JOIN te.testRun tr " +
           "WHERE tr.project.id = :projectId AND te.status IN ('FAIL','ERROR') " +
           "AND tr.createdAt >= :since ORDER BY tr.createdAt DESC")
    List<TestExecution> findRecentFailures(@Param("projectId") Long projectId,
                                           @Param("since") Instant since);

    @Query(value = "SELECT DISTINCT CONCAT(te.suite_name,'.',te.class_name,'.',te.test_name) " +
           "FROM test_executions te JOIN test_runs tr ON te.test_run_id=tr.id " +
           "WHERE tr.project_id=:projectId AND tr.created_at>=:since",
           nativeQuery = true)
    List<String> findDistinctTestCaseKeys(@Param("projectId") Long projectId,
                                          @Param("since") Instant since);

    @Query("SELECT te FROM TestExecution te JOIN te.testRun tr " +
           "WHERE tr.project.id=:projectId " +
           "AND CONCAT(te.suiteName,'.',te.className,'.',te.testName)=:key " +
           "AND tr.createdAt>=:since ORDER BY tr.createdAt DESC")
    List<TestExecution> findByTestCaseKey(@Param("projectId") Long projectId,
                                          @Param("key") String key,
                                          @Param("since") Instant since);
}
