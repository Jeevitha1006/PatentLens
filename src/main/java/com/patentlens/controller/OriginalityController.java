package com.patentlens.controller;

import com.patentlens.entity.AnalysisHistoryEntity;
import com.patentlens.model.OriginalityMetricResult;
import com.patentlens.repository.AnalysisHistoryRepository;
import com.patentlens.service.OriginalityService;
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
public class OriginalityController {

    private final OriginalityService originalityService;
    private final AnalysisHistoryRepository analysisHistoryRepository;

    public OriginalityController(OriginalityService originalityService, AnalysisHistoryRepository analysisHistoryRepository) {
        this.originalityService = originalityService;
        this.analysisHistoryRepository = analysisHistoryRepository;
    }

    @GetMapping("/originality")
    public String showOriginalityPage(@RequestParam(name = "sample", required = false) Boolean sample, Model model) {
        if (Boolean.TRUE.equals(sample)) {
            try {
                String sampleDraft = loadClasspathSample("samples/patent_draft_new.txt");
                OriginalityMetricResult result = originalityService.analyzeOriginality(sampleDraft);

                model.addAttribute("documentText", sampleDraft);
                model.addAttribute("originalityResult", result);
                model.addAttribute("isSample", true);

                analysisHistoryRepository.save(new AnalysisHistoryEntity(
                        "ORIGINALITY",
                        "Originality & Stylometry Audit (Sample Demo)",
                        "Risk Score: " + result.getAiLikenessRiskScore() + "/100 (" + result.getRiskCategory() +
                        "). Burstiness: " + result.getSentenceLengthVariance() + ", TTR: " + result.getTypeTokenRatio() + "."
                ));
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Could not load sample draft: " + e.getMessage());
            }
        }
        return "originality";
    }

    @PostMapping("/originality")
    public String analyzeOriginality(
            @RequestParam(name = "documentFile", required = false) MultipartFile documentFile,
            @RequestParam(name = "documentText", required = false) String documentText,
            Model model) {

        try {
            String extracted = documentFile != null && !documentFile.isEmpty()
                    ? DocumentParserUtil.extractText(documentFile)
                    : documentText;

            if (extracted == null || extracted.isBlank()) {
                model.addAttribute("errorMessage", "Please provide patent text or upload a document to evaluate stylometry metrics.");
                return "originality";
            }

            OriginalityMetricResult result = originalityService.analyzeOriginality(extracted);

            model.addAttribute("documentText", extracted);
            model.addAttribute("originalityResult", result);

            analysisHistoryRepository.save(new AnalysisHistoryEntity(
                    "ORIGINALITY",
                    "Originality & Stylometry Audit",
                    "Score: " + result.getAiLikenessRiskScore() + "/100 (" + result.getRiskCategory() + ")."
            ));

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error evaluating originality: " + e.getMessage());
        }

        return "originality";
    }

    private String loadClasspathSample(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
