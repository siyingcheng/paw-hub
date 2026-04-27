package com.pawhub.module.auth.repository;

import com.pawhub.module.auth.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByUserId(Long userId);
    Optional<Membership> findByUserIdAndTeamId(Long userId, Long teamId);
    boolean existsByUserIdAndTeamId(Long userId, Long teamId);
}
