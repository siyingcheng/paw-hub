package com.pawhub.module.analysis.dto;
import java.util.List;

public record TrendResponse(String environment, String periodType, List<TrendDataPoint> dataPoints) {}
