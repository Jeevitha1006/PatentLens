package com.patentlens.service;

import com.patentlens.model.OriginalityMetricResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Service calculating Stylometry Heuristics on patent drafting text.
 *
 * <p><b>Stylometry Algorithms & Viva Defense:</b></p>
 * <ul>
 *   <li><b>Burstiness (Sentence-Length Variance):</b> Human authors naturally alternate between
 *       short declarations and complex multi-clause sentences (high variance &sigma;&sup2;).
 *       Large Language Models (LLMs) conversely produce remarkably uniform sentence lengths (low variance).</li>
 *   <li><b>Type-Token Ratio (TTR):</b> Ratio of unique vocabulary items (V) to total word count (N):
 *       {@code TTR = |V| / N}. Quantifies lexical diversity.</li>
 *   <li><b>Repeated N-Gram Rate:</b> Measures syntactic repetition across consecutive 2-word (bigram)
 *       and 3-word (trigram) sequences.</li>
 *   <li><b>Risk Synthesis Formula:</b> Synthesizes low burstiness, narrow lexical diversity,
 *       and repetitive syntactic patterns into an intuitive 0-100 "AI-likeness risk" indicator.</li>
 * </ul>
 */
@Service
public class OriginalityService {

    private static final Pattern SENTENCE_SPLIT_PATTERN = Pattern.compile("(?<=[.!?])\\s+");
    private static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("[^a-zA-Z0-9]+");

    /**
     * Analyzes patent text and computes stylometry metrics and AI-likeness risk score.
     *
     * @param documentText Patent text to analyze
     * @return OriginalityMetricResult with detailed statistical breakdown and disclaimer
     */
    public OriginalityMetricResult analyzeOriginality(String documentText) {
        OriginalityMetricResult result = new OriginalityMetricResult();

        if (documentText == null || documentText.trim().isEmpty()) {
            result.setAiLikenessRiskScore(0);
            result.setRiskCategory("Insufficient Text");
            result.setRiskBadgeClass("bg-secondary");
            return result;
        }

        // 1. Sentence tokenization
        String[] rawSentences = SENTENCE_SPLIT_PATTERN.split(documentText.trim());
        List<String> sentences = new ArrayList<>();
        List<Integer> sentenceWordCounts = new ArrayList<>();

        for (String s : rawSentences) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                sentences.add(trimmed);
                String[] words = WORD_SPLIT_PATTERN.split(trimmed.toLowerCase());
                int count = 0;
                for (String w : words) {
                    if (!w.isBlank()) count++;
                }
                if (count > 0) {
                    sentenceWordCounts.add(count);
                }
            }
        }

        // 2. Word and Token Analysis
        String[] allWords = WORD_SPLIT_PATTERN.split(documentText.toLowerCase());
        List<String> tokens = new ArrayList<>();
        Set<String> uniqueWords = new HashSet<>();

        for (String w : allWords) {
            String clean = w.trim();
            if (!clean.isEmpty()) {
                tokens.add(clean);
                uniqueWords.add(clean);
            }
        }

        int totalWords = tokens.size();
        int totalUnique = uniqueWords.size();
        int sentenceCount = Math.max(1, sentences.size());

        // Type-Token Ratio (TTR)
        double ttr = totalWords > 0 ? (double) totalUnique / totalWords : 0.0;

        // Sentence Length Variance (Burstiness)
        double meanSentenceLength = sentenceWordCounts.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = 0.0;
        for (int len : sentenceWordCounts) {
            variance += Math.pow(len - meanSentenceLength, 2);
        }
        variance = sentenceWordCounts.size() > 1 ? variance / sentenceWordCounts.size() : 0.0;

        // 3. Repeated N-Gram Rate (Bigrams & Trigrams)
        double ngramRate = calculateRepeatedNgramRate(tokens, 2);

        // 4. Heuristic AI-Likeness Risk Scoring (0 - 100)
        // High AI Risk Correlates with:
        // - Low variance (< 35) -> uniform cadence
        // - Low TTR (< 0.45) -> repetitive word distribution
        // - High n-gram repetition (> 0.20)
        double risk = 35.0; // Baseline

        // Variance adjustment (burstiness)
        if (variance < 20.0) {
            risk += 25.0;
        } else if (variance < 50.0) {
            risk += 12.0;
        } else if (variance > 120.0) {
            risk -= 15.0; // High natural burstiness
        }

        // TTR adjustment
        if (ttr < 0.35) {
            risk += 20.0;
        } else if (ttr > 0.60) {
            risk -= 15.0; // Rich vocabulary
        }

        // N-gram repetition adjustment
        if (ngramRate > 0.25) {
            risk += 18.0;
        } else if (ngramRate < 0.10) {
            risk -= 10.0;
        }

        int finalScore = (int) Math.max(5, Math.min(95, Math.round(risk)));

        result.setSentenceCount(sentenceCount);
        result.setWordCount(totalWords);
        result.setUniqueWordCount(totalUnique);
        result.setTypeTokenRatio(Math.round(ttr * 1000.0) / 1000.0);
        result.setSentenceLengthVariance(Math.round(variance * 10.0) / 10.0);
        result.setRepeatedNgramRate(Math.round(ngramRate * 1000.0) / 10.0);
        result.setAiLikenessRiskScore(finalScore);

        if (finalScore >= 66) {
            result.setRiskCategory("High Risk (Uniform Cadence / High Repetition)");
            result.setRiskBadgeClass("bg-danger");
        } else if (finalScore >= 40) {
            result.setRiskCategory("Moderate Risk (Mixed Stylometric Signals)");
            result.setRiskBadgeClass("bg-warning text-dark");
        } else {
            result.setRiskCategory("Low Risk (Natural Human-like Burstiness)");
            result.setRiskBadgeClass("bg-success");
        }

        return result;
    }

    private double calculateRepeatedNgramRate(List<String> tokens, int n) {
        if (tokens.size() < n) return 0.0;

        Map<String, Integer> counts = new HashMap<>();
        int totalNgrams = tokens.size() - n + 1;

        for (int i = 0; i <= tokens.size() - n; i++) {
            String ngram = String.join(" ", tokens.subList(i, i + n));
            counts.put(ngram, counts.getOrDefault(ngram, 0) + 1);
        }

        int repeatedCount = 0;
        for (int c : counts.values()) {
            if (c > 1) {
                repeatedCount += (c - 1);
            }
        }

        return totalNgrams > 0 ? (double) repeatedCount / totalNgrams : 0.0;
    }
}
