package com.patentlens.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the overall TF-IDF novelty analysis across all claims in the patent.
 */
public class NoveltyScoreResult {

    private List<ClaimNoveltyComparison> claimComparisons = new ArrayList<>();
    private double averageNovelty;
    private double averageOldNovelty;
    private double overallNoveltyImprovement;
    private List<String> topPriorArtKeywords = new ArrayList<>();

    public NoveltyScoreResult() {
    }

    public List<ClaimNoveltyComparison> getClaimComparisons() {
        return claimComparisons;
    }

    public void setClaimComparisons(List<ClaimNoveltyComparison> claimComparisons) {
        this.claimComparisons = claimComparisons;
    }

    public double getAverageNovelty() {
        return averageNovelty;
    }

    public void setAverageNovelty(double averageNovelty) {
        this.averageNovelty = averageNovelty;
    }

    public double getAverageOldNovelty() {
        return averageOldNovelty;
    }

    public void setAverageOldNovelty(double averageOldNovelty) {
        this.averageOldNovelty = averageOldNovelty;
    }

    public double getOverallNoveltyImprovement() {
        return overallNoveltyImprovement;
    }

    public void setOverallNoveltyImprovement(double overallNoveltyImprovement) {
        this.overallNoveltyImprovement = overallNoveltyImprovement;
    }

    public List<String> getTopPriorArtKeywords() {
        return topPriorArtKeywords;
    }

    public void setTopPriorArtKeywords(List<String> topPriorArtKeywords) {
        this.topPriorArtKeywords = topPriorArtKeywords;
    }
}
