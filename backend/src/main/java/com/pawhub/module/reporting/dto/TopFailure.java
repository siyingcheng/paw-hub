package com.pawhub.module.reporting.dto;

public record TopFailure(String testName, String errorMessage, long failCount) {}
