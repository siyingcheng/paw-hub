package com.pawhub.module.analysis.dto;
import java.time.LocalDate;

public record TrendDataPoint(LocalDate date, double passRate, double failureRate, double avgDurationMs, double retryRate) {}
