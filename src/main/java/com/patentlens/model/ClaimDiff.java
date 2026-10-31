package com.patentlens.model;

/**
 * Represents the diff status and text comparison for an individual patent claim.
 */
public class ClaimDiff {

    private int claimNumber;
    private String status; // "ADDED", "REMOVED", "MODIFIED", "UNCHANGED"
    private String oldText;
    private String newText;
    private String diffHtml;

    public ClaimDiff() {
    }

    public ClaimDiff(int claimNumber, String status, String oldText, String newText, String diffHtml) {
        this.claimNumber = claimNumber;
        this.status = status;
        this.oldText = oldText;
        this.newText = newText;
        this.diffHtml = diffHtml;
    }

    public int getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(int claimNumber) {
        this.claimNumber = claimNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOldText() {
        return oldText;
    }

    public void setOldText(String oldText) {
        this.oldText = oldText;
    }

    public String getNewText() {
        return newText;
    }

    public void setNewText(String newText) {
        this.newText = newText;
    }

    public String getDiffHtml() {
        return diffHtml;
    }

    public void setDiffHtml(String diffHtml) {
        this.diffHtml = diffHtml;
    }

    public String getBadgeClass() {
        if ("ADDED".equalsIgnoreCase(status)) return "bg-success";
        if ("REMOVED".equalsIgnoreCase(status)) return "bg-danger";
        if ("MODIFIED".equalsIgnoreCase(status)) return "bg-warning text-dark";
        return "bg-secondary";
    }
}
