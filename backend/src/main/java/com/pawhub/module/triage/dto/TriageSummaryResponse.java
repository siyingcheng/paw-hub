package com.pawhub.module.triage.dto;

import com.pawhub.module.triage.entity.FailureTriage;
import java.util.Map;

public record TriageSummaryResponse(long totalTriaged, long untriaged, Map<FailureTriage.TriageStatus, Long> breakdown) {}
