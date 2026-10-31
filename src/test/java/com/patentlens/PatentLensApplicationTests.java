package com.patentlens;

import com.patentlens.model.ClaimDiffResult;
import com.patentlens.model.DetectedRejection;
import com.patentlens.model.NoveltyScoreResult;
import com.patentlens.model.OriginalityMetricResult;
import com.patentlens.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PatentLensApplicationTests {

    @Autowired
    private ClaimExtractionService claimExtractionService;

    @Autowired
    private ClaimDiffService claimDiffService;

    @Autowired
    private RejectionDetectorService rejectionDetectorService;

    @Autowired
    private TfIdfService tfIdfService;

    @Autowired
    private NoveltyScoringService noveltyScoringService;

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private OriginalityService originalityService;

    @Test
    void contextLoads() {
        assertNotNull(claimExtractionService);
        assertNotNull(blockchainService);
    }

    @Test
    void testClaimExtractionAndDiff() {
        String oldDraft = "1. An autonomous drone with GPS.\n2. The system of claim 1 with camera.";
        String newDraft = "1. An autonomous drone with dual GPS and LiDAR.\n2. The system of claim 1 with camera.\n3. A new claim.";

        ClaimDiffResult diff = claimDiffService.comparePatentDrafts(oldDraft, newDraft);
        assertEquals(2, diff.getTotalOldClaims());
        assertEquals(3, diff.getTotalNewClaims());
        assertEquals(1, diff.getAddedCount());
        assertEquals(1, diff.getModifiedCount());
        assertEquals(1, diff.getUnchangedCount());
    }

    @Test
    void testRejectionDetection() {
        String officeAction = "Claims 1-2 are rejected under 35 U.S.C. 102 as anticipated by US 2019/0084567 Smith et al. " +
                "Claim 3 is rejected under 35 U.S.C. 103 over Smith in view of Johnson.";

        List<DetectedRejection> rejections = rejectionDetectorService.detectRejections(officeAction);
        assertFalse(rejections.isEmpty());
        assertTrue(rejections.stream().anyMatch(r -> r.getGround().contains("102")));
    }

    @Test
    void testTfIdfCosineSimilarity() {
        List<String> doc1 = tfIdfService.tokenize("autonomous drone sensor navigation delivery");
        List<String> doc2 = tfIdfService.tokenize("autonomous drone sensor navigation delivery");
        var idf = tfIdfService.computeIdf(List.of(doc1, doc2));
        var vec1 = tfIdfService.computeTfIdfVector(doc1, idf);
        var vec2 = tfIdfService.computeTfIdfVector(doc2, idf);

        double similarity = tfIdfService.cosineSimilarity(vec1, vec2);
        assertTrue(similarity > 0.95);
    }

    @Test
    void testBlockchainIntegrity() {
        blockchainService.timestampDocument("Draft Claim 1: Drone payload release.", "Draft 1");
        BlockchainService.IntegrityReport report = blockchainService.verifyChainIntegrity();
        assertTrue(report.isValid());
    }

    @Test
    void testOriginalityMetrics() {
        String sampleText = "The system navigates safely. Furthermore, sensor arrays synthesize point clouds continuously.";
        OriginalityMetricResult result = originalityService.analyzeOriginality(sampleText);
        assertTrue(result.getAiLikenessRiskScore() >= 0 && result.getAiLikenessRiskScore() <= 100);
        assertTrue(result.getWordCount() > 5);
    }
}
