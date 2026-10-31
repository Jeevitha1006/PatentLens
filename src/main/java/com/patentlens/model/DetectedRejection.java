package com.patentlens.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a detected rejection ground (101, 102, 103, 112)
 * extracted from an Examiner's Office Action.
 */
public class DetectedRejection {

    private String ground; // e.g. "35 U.S.C. 101", "35 U.S.C. 102", "35 U.S.C. 103", "35 U.S.C. 112"
    private String rejectedClaims;
    private List<String> citedPriorArt = new ArrayList<>();
    private String rawExcerpt;
    private String plainReason;
    private String suggestedAmendment;
    private String overcomingArgument;

    public DetectedRejection() {
    }

    public DetectedRejection(String ground, String rejectedClaims, List<String> citedPriorArt, String rawExcerpt) {
        this.ground = ground;
        this.rejectedClaims = rejectedClaims;
        this.citedPriorArt = citedPriorArt;
        this.rawExcerpt = rawExcerpt;
    }

    public String getGround() {
        return ground;
    }

    public void setGround(String ground) {
        this.ground = ground;
    }

    public String getRejectedClaims() {
        return rejectedClaims;
    }

    public void setRejectedClaims(String rejectedClaims) {
        this.rejectedClaims = rejectedClaims;
    }

    public List<String> getCitedPriorArt() {
        return citedPriorArt;
    }

    public void setCitedPriorArt(List<String> citedPriorArt) {
        this.citedPriorArt = citedPriorArt;
    }

    public String getRawExcerpt() {
        return rawExcerpt;
    }

    public void setRawExcerpt(String rawExcerpt) {
        this.rawExcerpt = rawExcerpt;
    }

    public String getPlainReason() {
        return plainReason;
    }

    public void setPlainReason(String plainReason) {
        this.plainReason = plainReason;
    }

    public String getSuggestedAmendment() {
        return suggestedAmendment;
    }

    public void setSuggestedAmendment(String suggestedAmendment) {
        this.suggestedAmendment = suggestedAmendment;
    }

    public String getOvercomingArgument() {
        return overcomingArgument;
    }

    public void setOvercomingArgument(String overcomingArgument) {
        this.overcomingArgument = overcomingArgument;
    }

    public String getBadgeClass() {
        if (ground != null) {
            if (ground.contains("101")) return "bg-danger";
            if (ground.contains("102")) return "bg-warning text-dark";
            if (ground.contains("103")) return "bg-info text-dark";
            if (ground.contains("112")) return "bg-secondary";
        }
        return "bg-primary";
    }
}
