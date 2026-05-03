package com.pawhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analysis")
public class AnalysisProperties {
    private double flakyThreshold;
    private double regressionSigma;
    private int windowDays;
    private int consecutivePassCount;

    public double getFlakyThreshold() { return flakyThreshold; }
    public void setFlakyThreshold(double v) { this.flakyThreshold = v; }
    public double getRegressionSigma() { return regressionSigma; }
    public void setRegressionSigma(double v) { this.regressionSigma = v; }
    public int getWindowDays() { return windowDays; }
    public void setWindowDays(int v) { this.windowDays = v; }
    public int getConsecutivePassCount() { return consecutivePassCount; }
    public void setConsecutivePassCount(int v) { this.consecutivePassCount = v; }
}
