package com.patentlens.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of analyzing an office action: detected rejections and LLM explanations.
 */
public class RejectionAnalysis {

    private List<DetectedRejection> detectedRejections = new ArrayList<>();
    private int totalRejections;
    private String overallStrategy;
    private boolean usedMock;

    public RejectionAnalysis() {
    }

    public List<DetectedRejection> getDetectedRejections() {
        return detectedRejections;
    }

    public void setDetectedRejections(List<DetectedRejection> detectedRejections) {
        this.detectedRejections = detectedRejections;
        this.totalRejections = detectedRejections != null ? detectedRejections.size() : 0;
    }

    public int getTotalRejections() {
        return totalRejections;
    }

    public void setTotalRejections(int totalRejections) {
        this.totalRejections = totalRejections;
    }

    public String getOverallStrategy() {
        return overallStrategy;
    }

    public void setOverallStrategy(String overallStrategy) {
        this.overallStrategy = overallStrategy;
    }

    public boolean isUsedMock() {
        return usedMock;
    }

    public void setUsedMock(boolean usedMock) {
        this.usedMock = usedMock;
    }
}
