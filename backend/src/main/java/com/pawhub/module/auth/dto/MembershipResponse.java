package com.pawhub.module.auth.dto;

import com.pawhub.module.auth.entity.Membership;

public record MembershipResponse(Long userId, Long teamId, String role) {
    public static MembershipResponse from(Membership m) {
        return new MembershipResponse(m.getUser().getId(), m.getTeam().getId(), m.getRole().name());
    }
}
