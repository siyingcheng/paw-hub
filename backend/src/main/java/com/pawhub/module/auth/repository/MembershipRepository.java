package com.pawhub.module.auth.repository;

import com.pawhub.module.auth.entity.Membership;
import com.pawhub.module.auth.entity.MembershipRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByUserId(Long userId);
    List<Membership> findByTeamId(Long teamId);
    Optional<Membership> findByUserIdAndTeamId(Long userId, Long teamId);
    boolean existsByUserIdAndTeamId(Long userId, Long teamId);
    long countByTeamIdAndRole(Long teamId, MembershipRole role);
}
