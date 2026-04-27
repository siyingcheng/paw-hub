package com.pawhub.module.analysis.dto;

import java.util.List;

public record RegressionResponse(boolean runLevelRegression, String runLevelDetail, List<String> regressedCases) {}
