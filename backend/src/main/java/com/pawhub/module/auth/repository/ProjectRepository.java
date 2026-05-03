package com.pawhub.module.auth.repository;

import com.pawhub.module.auth.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByTeamId(Long teamId);

    @Query("SELECT p FROM Project p JOIN FETCH p.team WHERE p.id = :id")
    Optional<Project> findByIdWithTeam(@Param("id") Long id);
}
