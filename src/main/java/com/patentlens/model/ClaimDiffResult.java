package com.patentlens.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the overall result of comparing old vs new patent draft claims.
 */
public class ClaimDiffResult {

    private int totalOldClaims;
    private int totalNewClaims;
    private int addedCount;
    private int removedCount;
    private int modifiedCount;
    private int unchangedCount;
    private List<ClaimDiff> claimDiffs = new ArrayList<>();

    public ClaimDiffResult() {
    }

    public int getTotalOldClaims() {
        return totalOldClaims;
    }

    public void setTotalOldClaims(int totalOldClaims) {
        this.totalOldClaims = totalOldClaims;
    }

    public int getTotalNewClaims() {
        return totalNewClaims;
    }

    public void setTotalNewClaims(int totalNewClaims) {
        this.totalNewClaims = totalNewClaims;
    }

    public int getAddedCount() {
        return addedCount;
    }

    public void setAddedCount(int addedCount) {
        this.addedCount = addedCount;
    }

    public int getRemovedCount() {
        return removedCount;
    }

    public void setRemovedCount(int removedCount) {
        this.removedCount = removedCount;
    }

    public int getModifiedCount() {
        return modifiedCount;
    }

    public void setModifiedCount(int modifiedCount) {
        this.modifiedCount = modifiedCount;
    }

    public int getUnchangedCount() {
        return unchangedCount;
    }

    public void setUnchangedCount(int unchangedCount) {
        this.unchangedCount = unchangedCount;
    }

    public List<ClaimDiff> getClaimDiffs() {
        return claimDiffs;
    }

    public void setClaimDiffs(List<ClaimDiff> claimDiffs) {
        this.claimDiffs = claimDiffs;
    }
}
