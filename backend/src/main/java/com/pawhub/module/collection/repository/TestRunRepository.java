package com.pawhub.module.collection.repository;

import com.pawhub.module.collection.entity.TestRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface TestRunRepository extends JpaRepository<TestRun, Long> {
    Page<TestRun> findByProjectId(Long projectId, Pageable pageable);

    List<TestRun> findByProjectIdAndEnvironmentAndCreatedAtBetween(
        Long projectId, String environment, Instant from, Instant to);
}
