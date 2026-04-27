package com.pawhub.module.analysis.repository;
import com.pawhub.module.analysis.entity.FlakyTestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FlakyTestRecordRepository extends JpaRepository<FlakyTestRecord, Long> {
    List<FlakyTestRecord> findByProjectIdOrderByFlakyScoreDesc(Long projectId);
    Optional<FlakyTestRecord> findByProjectIdAndTestCaseKey(Long projectId, String testCaseKey);
}
