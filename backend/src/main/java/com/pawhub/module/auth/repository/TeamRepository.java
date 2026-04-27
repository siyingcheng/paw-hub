package com.pawhub.module.auth.repository;

import com.pawhub.module.auth.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByOrganizationId(Long orgId);
}
