package com.patentlens.model;

/**
 * Result of computing stylometry heuristics on patent drafting text:
 * sentence-length variance, type-token ratio (TTR), repeated n-grams, and AI-likeness risk.
 */
public class OriginalityMetricResult {

    private int aiLikenessRiskScore; // 0 - 100
    private String riskCategory; // "Low Risk (Natural / Human-like)", "Moderate Risk", "High Risk (Likely AI-Generated)"
    private String riskBadgeClass;
    private int sentenceCount;
    private int wordCount;
    private int uniqueWordCount;
    private double typeTokenRatio; // TTR
    private double sentenceLengthVariance; // Burstiness
    private double repeatedNgramRate; // n-gram repetition
    private String disclaimer = "Drafting aid only, not legal advice. Stylometry heuristics provide probabilistic structural indicators and do not constitute legal proof of authorship.";

    public OriginalityMetricResult() {
    }

    public int getAiLikenessRiskScore() {
        return aiLikenessRiskScore;
    }

    public void setAiLikenessRiskScore(int aiLikenessRiskScore) {
        this.aiLikenessRiskScore = aiLikenessRiskScore;
    }

    public String getRiskCategory() {
        return riskCategory;
    }

    public void setRiskCategory(String riskCategory) {
        this.riskCategory = riskCategory;
    }

    public String getRiskBadgeClass() {
        return riskBadgeClass;
    }

    public void setRiskBadgeClass(String riskBadgeClass) {
        this.riskBadgeClass = riskBadgeClass;
    }

    public int getSentenceCount() {
        return sentenceCount;
    }

    public void setSentenceCount(int sentenceCount) {
        this.sentenceCount = sentenceCount;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getUniqueWordCount() {
        return uniqueWordCount;
    }

    public void setUniqueWordCount(int uniqueWordCount) {
        this.uniqueWordCount = uniqueWordCount;
    }

    public double getTypeTokenRatio() {
        return typeTokenRatio;
    }

    public void setTypeTokenRatio(double typeTokenRatio) {
        this.typeTokenRatio = typeTokenRatio;
    }

    public double getSentenceLengthVariance() {
        return sentenceLengthVariance;
    }

    public void setSentenceLengthVariance(double sentenceLengthVariance) {
        this.sentenceLengthVariance = sentenceLengthVariance;
    }

    public double getRepeatedNgramRate() {
        return repeatedNgramRate;
    }

    public void setRepeatedNgramRate(double repeatedNgramRate) {
        this.repeatedNgramRate = repeatedNgramRate;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
