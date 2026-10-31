package com.patentlens.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for extracting and normalizing patent claims from specification text.
 * Uses regular expressions to detect numbered claims across various patent drafting formats.
 */
@Service
public class ClaimExtractionService {

    // Regex pattern matching claim headings like "1.", "Claim 1.", "1:", "CLAIM 1 -", etc.
    private static final Pattern CLAIM_PATTERN = Pattern.compile(
            "(?m)^\\s*(?:(?:Claim|CLAIM)\\s+)?(\\d+)[\\.\\:\\-]\\s*([\\s\\S]*?)(?=(?:^\\s*(?:(?:Claim|CLAIM)\\s+)?\\d+[\\.\\:\\-])|\\z)"
    );

    /**
     * Splits patent draft text into a Map of claim number to claim body text.
     *
     * @param documentText Full patent draft text
     * @return Sorted map of Claim Number -> Claim Text
     */
    public Map<Integer, String> extractClaims(String documentText) {
        Map<Integer, String> claims = new TreeMap<>();
        if (documentText == null || documentText.trim().isEmpty()) {
            return claims;
        }

        // Isolate CLAIMS section if explicit heading is present
        String textToScan = documentText;
        int claimsIndex = findClaimsSectionIndex(documentText);
        if (claimsIndex != -1) {
            textToScan = documentText.substring(claimsIndex);
        }

        Matcher matcher = CLAIM_PATTERN.matcher(textToScan);
        while (matcher.find()) {
            try {
                int claimNum = Integer.parseInt(matcher.group(1).trim());
                String claimBody = matcher.group(2).trim();
                if (!claimBody.isEmpty()) {
                    claims.put(claimNum, claimBody);
                }
            } catch (NumberFormatException ignored) {
                // Skip invalid claim numbering
            }
        }

        // Fallback: If no numbered claims matched, treat entire text as Claim 1
        if (claims.isEmpty() && !documentText.trim().isEmpty()) {
            claims.put(1, documentText.trim());
        }

        return claims;
    }

    private int findClaimsSectionIndex(String text) {
        String lower = text.toLowerCase();
        int idx = lower.indexOf("what is claimed is:");
        if (idx != -1) return idx;
        idx = lower.indexOf("we claim:");
        if (idx != -1) return idx;
        idx = lower.indexOf("i claim:");
        if (idx != -1) return idx;
        idx = lower.indexOf("claims:");
        if (idx != -1) return idx;
        idx = lower.indexOf("claims");
        return idx;
    }
}
