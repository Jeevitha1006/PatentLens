package com.patentlens.model;

/**
 * Compares an individual claim's TF-IDF cosine similarity against prior art,
 * tracking change between OLD and NEW drafts.
 */
public class ClaimNoveltyComparison {

    private int claimNumber;
    private String claimText;
    private double oldSimilarity; // 0.0 - 1.0 (overlap with prior art)
    private double newSimilarity; // 0.0 - 1.0
    private double oldNoveltyScore; // 0 - 100% (1 - similarity) * 100
    private double newNoveltyScore; // 0 - 100%
    private double scoreDelta; // newNoveltyScore - oldNoveltyScore (positive is improvement)
    private String statusColor; // "success" (green), "warning" (yellow), "danger" (red)
    private String statusLabel; // e.g. "High Novelty", "Moderate Overlap", "High Prior-Art Overlap"

    public ClaimNoveltyComparison() {
    }

    public int getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(int claimNumber) {
        this.claimNumber = claimNumber;
    }

    public String getClaimText() {
        return claimText;
    }

    public void setClaimText(String claimText) {
        this.claimText = claimText;
    }

    public double getOldSimilarity() {
        return oldSimilarity;
    }

    public void setOldSimilarity(double oldSimilarity) {
        this.oldSimilarity = oldSimilarity;
    }

    public double getNewSimilarity() {
        return newSimilarity;
    }

    public void setNewSimilarity(double newSimilarity) {
        this.newSimilarity = newSimilarity;
    }

    public double getOldNoveltyScore() {
        return oldNoveltyScore;
    }

    public void setOldNoveltyScore(double oldNoveltyScore) {
        this.oldNoveltyScore = oldNoveltyScore;
    }

    public double getNewNoveltyScore() {
        return newNoveltyScore;
    }

    public void setNewNoveltyScore(double newNoveltyScore) {
        this.newNoveltyScore = newNoveltyScore;
    }

    public double getScoreDelta() {
        return scoreDelta;
    }

    public void setScoreDelta(double scoreDelta) {
        this.scoreDelta = scoreDelta;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }
}
