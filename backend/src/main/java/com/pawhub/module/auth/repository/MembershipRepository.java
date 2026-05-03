package com.pawhub.module.auth.repository;

import com.pawhub.module.auth.entity.Membership;
import com.pawhub.module.auth.entity.MembershipRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByUserId(Long userId);
    List<Membership> findByTeamId(Long teamId);
    Optional<Membership> findByUserIdAndTeamId(Long userId, Long teamId);
    boolean existsByUserIdAndTeamId(Long userId, Long teamId);
    long countByTeamIdAndRole(Long teamId, MembershipRole role);

    @Query("SELECT m FROM Membership m JOIN FETCH m.user WHERE m.team.id = :teamId")
    List<Membership> findByTeamIdWithUser(@Param("teamId") Long teamId);

    @Query("SELECT m FROM Membership m JOIN FETCH m.user WHERE m.user.id = :userId AND m.team.id = :teamId")
    Optional<Membership> findByUserIdAndTeamIdWithUser(@Param("userId") Long userId, @Param("teamId") Long teamId);

    @Query("SELECT COUNT(m) > 0 FROM Membership m " +
           "WHERE m.user.id = :userId AND m.team.id = " +
           "(SELECT p.team.id FROM Project p WHERE p.id = :projectId)")
    boolean existsByUserIdAndProjectTeamId(@Param("userId") Long userId, @Param("projectId") Long projectId);
}
