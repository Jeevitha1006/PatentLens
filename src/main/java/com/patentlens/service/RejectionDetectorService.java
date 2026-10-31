package com.patentlens.service;

import com.patentlens.model.DetectedRejection;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for analyzing USPTO / patent office actions using rule-based NLP.
 * Detects statutory grounds (35 U.S.C. 101, 102, 103, 112), affected claims, and cited prior art.
 */
@Service
public class RejectionDetectorService {

    // Regex to locate sections or paragraphs discussing specific statutory grounds
    private static final Pattern SECTION_SPLIT_PATTERN = Pattern.compile(
            "(?mi)(?=(?:(?:rejection\\s+under|claims?\\s+(?:is|are)\\s+rejected\\s+under|rejected\\s+under)\\s+35\\s*u\\.?s\\.?c\\.?)|(?:\\d+\\.\\s*rejection\\s+under))"
    );

    private static final Pattern GROUND_101 = Pattern.compile("(?i)(?:35\\s*u\\.?s\\.?c\\.?\\s*(?:§\\s*)?101|subject\\s+matter\\s+eligib|abstract\\s+idea|alice\\s+step)");
    private static final Pattern GROUND_102 = Pattern.compile("(?i)(?:35\\s*u\\.?s\\.?c\\.?\\s*(?:§\\s*)?102|anticipat(?:ed|ion))");
    private static final Pattern GROUND_103 = Pattern.compile("(?i)(?:35\\s*u\\.?s\\.?c\\.?\\s*(?:§\\s*)?103|obvious(?:ness)?|unpatentable\\s+over)");
    private static final Pattern GROUND_112 = Pattern.compile("(?i)(?:35\\s*u\\.?s\\.?c\\.?\\s*(?:§\\s*)?112|indefinite|antecedent\\s+basis|written\\s+description|enablement)");

    // Regex to find rejected claim numbers (e.g. "Claims 1-5", "Claim 3 is rejected", "Claims 1, 2, and 4")
    private static final Pattern CLAIMS_PATTERN = Pattern.compile(
            "(?i)claims?\\s+([\\d\\s\\,\\-\\–\\—andto]+?)(?:\\s+(?:is|are)\\s+rejected|\\s+rejected|\\s+under)"
    );

    // Regex to extract patent citations (e.g. "US 2019/0084567 A1", "Patent No. 9,123,456", "Smith et al.")
    private static final Pattern PRIOR_ART_PATTERN = Pattern.compile(
            "(?i)(?:US\\s*(?:Patent\\s*(?:Publication|Pub\\.?|No\\.?))?\\s*\\d{4}[\\/\\-]\\d{7}(?:\\s*[A-Z]\\d)?)|" +
            "(?:US\\s*Patent\\s*(?:No\\.?\\s*)?[\\d\\,]{7,10})|" +
            "(?:(?:[A-Z][a-z]+(?:\\s+and\\s+[A-Z][a-z]+)?|WO\\s*\\d{4}\\/\\d{6})\\s+et\\s+al\\.?)"
    );

    /**
     * Parses the office action text and returns a list of detected statutory rejections.
     *
     * @param officeActionText Raw text of the office action
     * @return List of DetectedRejection objects
     */
    public List<DetectedRejection> detectRejections(String officeActionText) {
        List<DetectedRejection> rejections = new ArrayList<>();
        if (officeActionText == null || officeActionText.trim().isEmpty()) {
            return rejections;
        }

        // Split text by potential rejection boundaries
        String[] sections = SECTION_SPLIT_PATTERN.split(officeActionText);
        if (sections.length <= 1) {
            // If split didn't find multiple boundaries, split by paragraphs or double newlines
            sections = officeActionText.split("(?m)\\n\\s*\\n");
        }

        for (String section : sections) {
            String trimmed = section.trim();
            if (trimmed.length() < 20) continue;

            String ground = identifyGround(trimmed);
            if (ground != null) {
                String claims = extractClaimsRejected(trimmed);
                List<String> priorArt = extractPriorArtCitations(trimmed);

                // Create detected rejection
                DetectedRejection rej = new DetectedRejection(ground, claims, priorArt, trimmed);
                rejections.add(rej);
            }
        }

        // Fallback: If section parser missed anything, scan the entire text for each ground
        if (rejections.isEmpty()) {
            scanEntireTextFallback(officeActionText, rejections);
        }

        return rejections;
    }

    private String identifyGround(String text) {
        if (GROUND_101.matcher(text).find()) return "35 U.S.C. § 101 (Eligibility / Abstract Idea)";
        if (GROUND_102.matcher(text).find()) return "35 U.S.C. § 102 (Anticipation / Lack of Novelty)";
        if (GROUND_103.matcher(text).find()) return "35 U.S.C. § 103 (Obviousness)";
        if (GROUND_112.matcher(text).find()) return "35 U.S.C. § 112 (Indefiniteness / Enablement)";
        return null;
    }

    private String extractClaimsRejected(String text) {
        Matcher matcher = CLAIMS_PATTERN.matcher(text);
        if (matcher.find()) {
            String claimsStr = matcher.group(1).trim();
            return "Claim(s) " + claimsStr;
        }
        return "Claims referenced in paragraph";
    }

    private List<String> extractPriorArtCitations(String text) {
        Set<String> citations = new LinkedHashSet<>();
        Matcher matcher = PRIOR_ART_PATTERN.matcher(text);
        while (matcher.find()) {
            citations.add(matcher.group().trim());
        }
        return new ArrayList<>(citations);
    }

    private void scanEntireTextFallback(String fullText, List<DetectedRejection> list) {
        if (GROUND_101.matcher(fullText).find()) {
            list.add(new DetectedRejection("35 U.S.C. § 101 (Eligibility)", "Claims cited in 101 section",
                    Collections.emptyList(), extractSnippet(fullText, "101")));
        }
        if (GROUND_102.matcher(fullText).find()) {
            list.add(new DetectedRejection("35 U.S.C. § 102 (Anticipation)", "Claims cited in 102 section",
                    extractPriorArtCitations(fullText), extractSnippet(fullText, "102")));
        }
        if (GROUND_103.matcher(fullText).find()) {
            list.add(new DetectedRejection("35 U.S.C. § 103 (Obviousness)", "Claims cited in 103 section",
                    extractPriorArtCitations(fullText), extractSnippet(fullText, "103")));
        }
        if (GROUND_112.matcher(fullText).find()) {
            list.add(new DetectedRejection("35 U.S.C. § 112 (Indefiniteness)", "Claims cited in 112 section",
                    Collections.emptyList(), extractSnippet(fullText, "112")));
        }
    }

    private String extractSnippet(String fullText, String keyword) {
        int idx = fullText.indexOf(keyword);
        if (idx == -1) return fullText.length() > 300 ? fullText.substring(0, 300) + "..." : fullText;
        int start = Math.max(0, idx - 80);
        int end = Math.min(fullText.length(), idx + 350);
        return (start > 0 ? "..." : "") + fullText.substring(start, end).trim() + (end < fullText.length() ? "..." : "");
    }
}
