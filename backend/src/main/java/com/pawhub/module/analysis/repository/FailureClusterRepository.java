package com.pawhub.module.analysis.repository;
import com.pawhub.module.analysis.entity.FailureCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FailureClusterRepository extends JpaRepository<FailureCluster, Long> {
    List<FailureCluster> findByProjectIdOrderByOccurrenceCountDesc(Long projectId);
    Optional<FailureCluster> findByProjectIdAndClusterKey(Long projectId, String clusterKey);
}
