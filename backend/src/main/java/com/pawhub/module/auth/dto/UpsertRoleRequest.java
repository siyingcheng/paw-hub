package com.pawhub.module.auth.dto;

import jakarta.validation.constraints.NotNull;
import com.pawhub.module.auth.entity.MembershipRole;

public record UpsertRoleRequest(@NotNull MembershipRole role) {}
