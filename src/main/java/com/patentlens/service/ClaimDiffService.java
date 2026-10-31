package com.patentlens.service;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.Patch;
import com.patentlens.model.ClaimDiff;
import com.patentlens.model.ClaimDiffResult;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for computing side-by-side claim differences between old and new patent drafts.
 * Highlights added, removed, and modified claims and words using java-diff-utils.
 */
@Service
public class ClaimDiffService {

    private final ClaimExtractionService claimExtractionService;

    public ClaimDiffService(ClaimExtractionService claimExtractionService) {
        this.claimExtractionService = claimExtractionService;
    }

    /**
     * Compares two patent specifications and generates structured claim-by-claim diff results.
     *
     * @param oldDraftText Old/original patent draft
     * @param newDraftText New/amended patent draft
     * @return ClaimDiffResult containing categorized claims and highlighted changes
     */
    public ClaimDiffResult comparePatentDrafts(String oldDraftText, String newDraftText) {
        Map<Integer, String> oldClaims = claimExtractionService.extractClaims(oldDraftText);
        Map<Integer, String> newClaims = claimExtractionService.extractClaims(newDraftText);

        Set<Integer> allClaimNumbers = new TreeSet<>();
        allClaimNumbers.addAll(oldClaims.keySet());
        allClaimNumbers.addAll(newClaims.keySet());

        ClaimDiffResult result = new ClaimDiffResult();
        result.setTotalOldClaims(oldClaims.size());
        result.setTotalNewClaims(newClaims.size());

        int added = 0;
        int removed = 0;
        int modified = 0;
        int unchanged = 0;

        List<ClaimDiff> diffList = new ArrayList<>();

        for (Integer claimNum : allClaimNumbers) {
            String oldText = oldClaims.get(claimNum);
            String newText = newClaims.get(claimNum);

            if (oldText == null && newText != null) {
                // Claim was newly added
                added++;
                String diffHtml = "<span class=\"text-success fw-semibold\">" + escapeHtml(newText) + "</span>";
                diffList.add(new ClaimDiff(claimNum, "ADDED", "", newText, diffHtml));
            } else if (oldText != null && newText == null) {
                // Claim was cancelled/removed
                removed++;
                String diffHtml = "<span class=\"text-danger text-decoration-line-through\">" + escapeHtml(oldText) + "</span>";
                diffList.add(new ClaimDiff(claimNum, "REMOVED", oldText, "", diffHtml));
            } else if (oldText != null && newText != null) {
                if (oldText.trim().equals(newText.trim())) {
                    // Claim is unchanged
                    unchanged++;
                    diffList.add(new ClaimDiff(claimNum, "UNCHANGED", oldText, newText, escapeHtml(newText)));
                } else {
                    // Claim was modified
                    modified++;
                    String highlightedDiff = generateWordLevelDiffHtml(oldText, newText);
                    diffList.add(new ClaimDiff(claimNum, "MODIFIED", oldText, newText, highlightedDiff));
                }
            }
        }

        result.setAddedCount(added);
        result.setRemovedCount(removed);
        result.setModifiedCount(modified);
        result.setUnchangedCount(unchanged);
        result.setClaimDiffs(diffList);

        return result;
    }

    /**
     * Generates HTML with inline highlights for added and deleted words using DiffUtils.
     */
    public String generateWordLevelDiffHtml(String oldText, String newText) {
        List<String> oldWords = Arrays.asList(oldText.split("\\s+"));
        List<String> newWords = Arrays.asList(newText.split("\\s+"));

        Patch<String> patch = DiffUtils.diff(oldWords, newWords);
        StringBuilder html = new StringBuilder();

        int lastIndex = 0;
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            Chunk<String> source = delta.getSource();
            Chunk<String> target = delta.getTarget();

            // Append unchanged words leading up to delta
            while (lastIndex < source.getPosition() && lastIndex < oldWords.size()) {
                html.append(escapeHtml(oldWords.get(lastIndex))).append(" ");
                lastIndex++;
            }

            switch (delta.getType()) {
                case DELETE:
                    html.append("<del class=\"px-1 bg-danger-subtle text-danger text-decoration-line-through rounded\">")
                        .append(escapeHtml(String.join(" ", source.getLines())))
                        .append("</del> ");
                    lastIndex += source.size();
                    break;

                case INSERT:
                    html.append("<ins class=\"px-1 bg-success-subtle text-success fw-semibold text-decoration-none rounded\">")
                        .append(escapeHtml(String.join(" ", target.getLines())))
                        .append("</ins> ");
                    break;

                case CHANGE:
                    html.append("<del class=\"px-1 bg-danger-subtle text-danger text-decoration-line-through rounded\">")
                        .append(escapeHtml(String.join(" ", source.getLines())))
                        .append("</del> ");
                    html.append("<ins class=\"px-1 bg-success-subtle text-success fw-semibold text-decoration-none rounded\">")
                        .append(escapeHtml(String.join(" ", target.getLines())))
                        .append("</ins> ");
                    lastIndex += source.size();
                    break;

                case EQUAL:
                default:
                    break;
            }
        }

        // Append remaining unchanged words
        while (lastIndex < oldWords.size()) {
            html.append(escapeHtml(oldWords.get(lastIndex))).append(" ");
            lastIndex++;
        }

        return html.toString().trim();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
