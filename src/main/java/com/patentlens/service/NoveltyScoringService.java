package com.patentlens.service;

import com.patentlens.model.ClaimNoveltyComparison;
import com.patentlens.model.NoveltyScoreResult;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for evaluating patent claim novelty and overlap against cited prior art using TF-IDF NLP.
 * Compares old draft vs amended draft to demonstrate whether amendments successfully reduced prior-art overlap.
 */
@Service
public class NoveltyScoringService {

    private final TfIdfService tfIdfService;
    private final ClaimExtractionService claimExtractionService;

    public NoveltyScoringService(TfIdfService tfIdfService, ClaimExtractionService claimExtractionService) {
        this.tfIdfService = tfIdfService;
        this.claimExtractionService = claimExtractionService;
    }

    /**
     * Scores claims in new draft against prior art, and compares with old draft claims if available.
     *
     * @param oldDraftText Text of old patent draft (optional, can be null or empty)
     * @param newDraftText Text of amended patent draft
     * @param priorArtText Text of cited prior art document(s)
     * @return NoveltyScoreResult with claim-by-claim heatmap data and delta comparisons
     */
    public NoveltyScoreResult evaluateNovelty(String oldDraftText, String newDraftText, String priorArtText) {
        Map<Integer, String> newClaims = claimExtractionService.extractClaims(newDraftText);
        Map<Integer, String> oldClaims = (oldDraftText != null && !oldDraftText.trim().isEmpty())
                ? claimExtractionService.extractClaims(oldDraftText)
                : Collections.emptyMap();

        // Build document corpus for global IDF calculation
        List<List<String>> corpusTokens = new ArrayList<>();
        List<String> priorArtTokens = tfIdfService.tokenize(priorArtText);
        corpusTokens.add(priorArtTokens);

        for (String cText : newClaims.values()) {
            corpusTokens.add(tfIdfService.tokenize(cText));
        }
        for (String cText : oldClaims.values()) {
            corpusTokens.add(tfIdfService.tokenize(cText));
        }

        Map<String, Double> idfMap = tfIdfService.computeIdf(corpusTokens);
        Map<String, Double> priorArtVector = tfIdfService.computeTfIdfVector(priorArtTokens, idfMap);

        List<ClaimNoveltyComparison> comparisons = new ArrayList<>();
        double totalNewNovelty = 0.0;
        double totalOldNovelty = 0.0;
        int evaluatedClaimsCount = 0;

        for (Map.Entry<Integer, String> entry : newClaims.entrySet()) {
            int claimNum = entry.getKey();
            String newText = entry.getValue();

            // Calculate similarity for new claim against prior art
            List<String> newTokens = tfIdfService.tokenize(newText);
            Map<String, Double> newVector = tfIdfService.computeTfIdfVector(newTokens, idfMap);
            double newSimilarity = tfIdfService.cosineSimilarity(newVector, priorArtVector);
            // Novelty is inverse of overlap: Novelty% = (1 - similarity) * 100
            double newNoveltyScore = Math.round((1.0 - newSimilarity) * 1000.0) / 10.0;

            // Calculate similarity for old claim if present
            double oldSimilarity = 0.0;
            double oldNoveltyScore = 0.0;
            if (oldClaims.containsKey(claimNum)) {
                String oldText = oldClaims.get(claimNum);
                List<String> oldTokens = tfIdfService.tokenize(oldText);
                Map<String, Double> oldVector = tfIdfService.computeTfIdfVector(oldTokens, idfMap);
                oldSimilarity = tfIdfService.cosineSimilarity(oldVector, priorArtVector);
                oldNoveltyScore = Math.round((1.0 - oldSimilarity) * 1000.0) / 10.0;
            } else {
                // If claim was newly added in new draft, default baseline novelty is moderate
                oldNoveltyScore = 50.0;
                oldSimilarity = 0.50;
            }

            double delta = Math.round((newNoveltyScore - oldNoveltyScore) * 10.0) / 10.0;

            ClaimNoveltyComparison cmp = new ClaimNoveltyComparison();
            cmp.setClaimNumber(claimNum);
            cmp.setClaimText(newText);
            cmp.setNewSimilarity(Math.round(newSimilarity * 1000.0) / 10.0);
            cmp.setOldSimilarity(Math.round(oldSimilarity * 1000.0) / 10.0);
            cmp.setNewNoveltyScore(newNoveltyScore);
            cmp.setOldNoveltyScore(oldNoveltyScore);
            cmp.setScoreDelta(delta);

            // Assign color badge based on novelty threshold
            if (newNoveltyScore >= 70.0) {
                cmp.setStatusColor("success"); // Green
                cmp.setStatusLabel("High Novelty (Low Overlap)");
            } else if (newNoveltyScore >= 40.0) {
                cmp.setStatusColor("warning"); // Yellow
                cmp.setStatusLabel("Moderate Overlap");
            } else {
                cmp.setStatusColor("danger"); // Red
                cmp.setStatusLabel("High Prior-Art Overlap");
            }

            comparisons.add(cmp);
            totalNewNovelty += newNoveltyScore;
            totalOldNovelty += oldNoveltyScore;
            evaluatedClaimsCount++;
        }

        NoveltyScoreResult result = new NoveltyScoreResult();
        result.setClaimComparisons(comparisons);

        if (evaluatedClaimsCount > 0) {
            double avgNew = Math.round((totalNewNovelty / evaluatedClaimsCount) * 10.0) / 10.0;
            double avgOld = Math.round((totalOldNovelty / evaluatedClaimsCount) * 10.0) / 10.0;
            result.setAverageNovelty(avgNew);
            result.setAverageOldNovelty(avgOld);
            result.setOverallNoveltyImprovement(Math.round((avgNew - avgOld) * 10.0) / 10.0);
        }

        result.setTopPriorArtKeywords(tfIdfService.extractTopKeywords(priorArtText, 8));
        return result;
    }
}
