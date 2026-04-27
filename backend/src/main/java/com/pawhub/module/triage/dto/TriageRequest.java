package com.pawhub.module.triage.dto;

import com.pawhub.module.triage.entity.FailureTriage;
import jakarta.validation.constraints.NotNull;

public record TriageRequest(@NotNull FailureTriage.TriageStatus triageStatus, String issueLink, String comment) {}
