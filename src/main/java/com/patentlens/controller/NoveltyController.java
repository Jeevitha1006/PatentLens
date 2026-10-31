package com.patentlens.controller;

import com.patentlens.entity.AnalysisHistoryEntity;
import com.patentlens.model.NoveltyScoreResult;
import com.patentlens.repository.AnalysisHistoryRepository;
import com.patentlens.service.NoveltyScoringService;
import com.patentlens.util.DocumentParserUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Controller
public class NoveltyController {

    private final NoveltyScoringService noveltyScoringService;
    private final AnalysisHistoryRepository analysisHistoryRepository;

    public NoveltyController(NoveltyScoringService noveltyScoringService,
                             AnalysisHistoryRepository analysisHistoryRepository) {
        this.noveltyScoringService = noveltyScoringService;
        this.analysisHistoryRepository = analysisHistoryRepository;
    }

    @GetMapping("/novelty")
    public String showNoveltyPage(@RequestParam(name = "sample", required = false) Boolean sample, Model model) {
        if (Boolean.TRUE.equals(sample)) {
            try {
                String oldText = loadClasspathSample("samples/patent_draft_old.txt");
                String newText = loadClasspathSample("samples/patent_draft_new.txt");
                String priorArt = loadClasspathSample("samples/sample_prior_art.txt");

                NoveltyScoreResult result = noveltyScoringService.evaluateNovelty(oldText, newText, priorArt);

                model.addAttribute("oldText", oldText);
                model.addAttribute("newText", newText);
                model.addAttribute("priorArtText", priorArt);
                model.addAttribute("noveltyResult", result);
                model.addAttribute("isSample", true);

                analysisHistoryRepository.save(new AnalysisHistoryEntity(
                        "NOVELTY",
                        "TF-IDF Novelty Heatmap (Sample Demo)",
                        "Average novelty increased from " + result.getAverageOldNovelty() + "% to " +
                        result.getAverageNovelty() + "% (+ " + result.getOverallNoveltyImprovement() + "% improvement)."
                ));
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Could not load sample data: " + e.getMessage());
            }
        }
        return "novelty";
    }

    @PostMapping("/novelty")
    public String scoreNovelty(
            @RequestParam(name = "newFile", required = false) MultipartFile newFile,
            @RequestParam(name = "newText", required = false) String newText,
            @RequestParam(name = "oldFile", required = false) MultipartFile oldFile,
            @RequestParam(name = "oldText", required = false) String oldText,
            @RequestParam(name = "priorArtFile", required = false) MultipartFile priorArtFile,
            @RequestParam(name = "priorArtText", required = false) String priorArtText,
            Model model) {

        try {
            String extractedNew = newFile != null && !newFile.isEmpty() ? DocumentParserUtil.extractText(newFile) : newText;
            String extractedOld = oldFile != null && !oldFile.isEmpty() ? DocumentParserUtil.extractText(oldFile) : oldText;
            String extractedPriorArt = priorArtFile != null && !priorArtFile.isEmpty() ? DocumentParserUtil.extractText(priorArtFile) : priorArtText;

            if (extractedNew == null || extractedNew.isBlank() || extractedPriorArt == null || extractedPriorArt.isBlank()) {
                model.addAttribute("errorMessage", "Both the Amended Patent Draft and Prior Art text are required for novelty calculation.");
                return "novelty";
            }

            NoveltyScoreResult result = noveltyScoringService.evaluateNovelty(extractedOld, extractedNew, extractedPriorArt);

            model.addAttribute("newText", extractedNew);
            model.addAttribute("oldText", extractedOld);
            model.addAttribute("priorArtText", extractedPriorArt);
            model.addAttribute("noveltyResult", result);

            analysisHistoryRepository.save(new AnalysisHistoryEntity(
                    "NOVELTY",
                    "Claim Novelty Scoring",
                    "Computed TF-IDF cosine similarity for " + result.getClaimComparisons().size() +
                    " claims. Average Novelty: " + result.getAverageNovelty() + "%."
            ));

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error calculating novelty: " + e.getMessage());
        }

        return "novelty";
    }

    private String loadClasspathSample(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
