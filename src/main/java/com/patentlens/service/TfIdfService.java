package com.patentlens.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Natural Language Processing (NLP) Service implementing TF-IDF (Term Frequency - Inverse Document Frequency)
 * and Cosine Similarity in pure Java.
 *
 * <p><b>Mathematical Algorithm Overview for Viva/Presentation:</b></p>
 * <ol>
 *   <li><b>Preprocessing:</b> Tokenization, lowercasing, non-alphanumeric stripping, and stopword removal.</li>
 *   <li><b>Term Frequency (TF):</b> {@code TF(t, d) = count(t, d) / total_tokens(d)}</li>
 *   <li><b>Inverse Document Frequency (IDF):</b> {@code IDF(t, D) = ln((1 + |D|) / (1 + DF(t))) + 1}
 *       penalizes ubiquitous terms and boosts distinctive patent concepts.</li>
 *   <li><b>TF-IDF Vector Weight:</b> {@code Weight(t, d) = TF(t, d) * IDF(t, D)}</li>
 *   <li><b>Cosine Similarity:</b> Dot product of two L2-normalized vectors:
 *       {@code Cosine(A, B) = (A . B) / (||A|| * ||B||)}
 *       Measures semantic directional overlap between claim text and prior art corpus on a scale of [0.0, 1.0].</li>
 * </ol>
 */
@Service
public class TfIdfService {

    private static final Pattern WORD_PATTERN = Pattern.compile("[^a-zA-Z0-9]+");

    // Standard English and patent drafting stop words that carry minimal conceptual distinctiveness
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can",
            "cannot", "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from",
            "further", "had", "has", "have", "having", "he", "her", "here", "hers", "herself", "him", "himself",
            "his", "how", "i", "if", "in", "into", "is", "isn't", "it", "its", "itself", "me", "more", "most",
            "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our",
            "ours", "ourselves", "out", "over", "own", "same", "she", "should", "so", "some", "such", "than",
            "that", "the", "their", "theirs", "them", "themselves", "then", "there", "these", "they", "this",
            "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't", "we", "were",
            "what", "when", "where", "which", "while", "who", "whom", "why", "with", "would", "you", "your",
            // Patent drafting boilerplate noise terms
            "comprising", "comprises", "wherein", "said", "plurality", "configured", "thereof", "according",
            "claim", "claims", "system", "method", "apparatus", "device", "means", "step", "steps"
    );

    /**
     * Splits text into normalized, filtered tokens.
     */
    public List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] words = WORD_PATTERN.split(text.toLowerCase());
        List<String> tokens = new ArrayList<>();
        for (String w : words) {
            String trimmed = w.trim();
            if (trimmed.length() > 2 && !STOP_WORDS.contains(trimmed)) {
                tokens.add(trimmed);
            }
        }
        return tokens;
    }

    /**
     * Computes Term Frequency (TF) vector for a list of tokens.
     * TF(t) = count(t) / total_words
     */
    public Map<String, Double> computeTf(List<String> tokens) {
        Map<String, Double> tfMap = new HashMap<>();
        if (tokens.isEmpty()) return tfMap;

        double total = tokens.size();
        for (String t : tokens) {
            tfMap.put(t, tfMap.getOrDefault(t, 0.0) + 1.0);
        }
        for (Map.Entry<String, Double> entry : tfMap.entrySet()) {
            entry.setValue(entry.getValue() / total);
        }
        return tfMap;
    }

    /**
     * Computes Inverse Document Frequency (IDF) across a collection of documents.
     * IDF(t) = ln((1 + |D|) / (1 + DF(t))) + 1.0
     */
    public Map<String, Double> computeIdf(List<List<String>> documentTokens) {
        Map<String, Double> idfMap = new HashMap<>();
        int totalDocs = documentTokens.size();
        if (totalDocs == 0) return idfMap;

        // Count how many documents contain each term
        Map<String, Integer> docFreq = new HashMap<>();
        for (List<String> doc : documentTokens) {
            Set<String> uniqueTerms = new HashSet<>(doc);
            for (String term : uniqueTerms) {
                docFreq.put(term, docFreq.getOrDefault(term, 0) + 1);
            }
        }

        // Calculate smoothed IDF
        for (Map.Entry<String, Integer> entry : docFreq.entrySet()) {
            double idf = Math.log((1.0 + totalDocs) / (1.0 + entry.getValue())) + 1.0;
            idfMap.put(entry.getKey(), idf);
        }

        return idfMap;
    }

    /**
     * Computes the TF-IDF vector by multiplying TF * IDF for each token.
     */
    public Map<String, Double> computeTfIdfVector(List<String> docTokens, Map<String, Double> idfMap) {
        Map<String, Double> tf = computeTf(docTokens);
        Map<String, Double> tfIdf = new HashMap<>();
        for (Map.Entry<String, Double> entry : tf.entrySet()) {
            String term = entry.getKey();
            double idf = idfMap.getOrDefault(term, 1.0);
            tfIdf.put(term, entry.getValue() * idf);
        }
        return tfIdf;
    }

    /**
     * Calculates the Cosine Similarity between two term-weight vectors:
     * Cosine = (A . B) / (||A|| * ||B||)
     *
     * @return Value between 0.0 (completely orthogonal/different) and 1.0 (identical vector direction)
     */
    public double cosineSimilarity(Map<String, Double> vectorA, Map<String, Double> vectorB) {
        if (vectorA.isEmpty() || vectorB.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (double val : vectorA.values()) {
            normA += val * val;
        }
        for (double val : vectorB.values()) {
            normB += val * val;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        // Compute dot product on common vocabulary terms
        for (Map.Entry<String, Double> entry : vectorA.entrySet()) {
            String term = entry.getKey();
            if (vectorB.containsKey(term)) {
                dotProduct += entry.getValue() * vectorB.get(term);
            }
        }

        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        return Math.max(0.0, Math.min(1.0, similarity)); // Clamp to [0, 1]
    }

    /**
     * Extracts top distinctive keywords from a corpus based on highest cumulative TF-IDF weights.
     */
    public List<String> extractTopKeywords(String text, int topN) {
        List<String> tokens = tokenize(text);
        if (tokens.isEmpty()) return Collections.emptyList();

        Map<String, Double> tf = computeTf(tokens);
        return tf.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
