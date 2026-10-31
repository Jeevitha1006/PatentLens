package com.patentlens.controller;

import com.patentlens.entity.AnalysisHistoryEntity;
import com.patentlens.model.DetectedRejection;
import com.patentlens.model.RejectionAnalysis;
import com.patentlens.repository.AnalysisHistoryRepository;
import com.patentlens.service.LlmService;
import com.patentlens.service.RejectionDetectorService;
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
import java.util.List;

@Controller
public class RejectionController {

    private final RejectionDetectorService rejectionDetectorService;
    private final LlmService llmService;
    private final AnalysisHistoryRepository analysisHistoryRepository;

    public RejectionController(RejectionDetectorService rejectionDetectorService,
                               LlmService llmService,
                               AnalysisHistoryRepository analysisHistoryRepository) {
        this.rejectionDetectorService = rejectionDetectorService;
        this.llmService = llmService;
        this.analysisHistoryRepository = analysisHistoryRepository;
    }

    @GetMapping("/rejection")
    public String showRejectionPage(@RequestParam(name = "sample", required = false) Boolean sample, Model model) {
        if (Boolean.TRUE.equals(sample)) {
            try {
                String sampleOfficeAction = loadClasspathSample("samples/sample_office_action.txt");
                String samplePatentDraft = loadClasspathSample("samples/patent_draft_old.txt");

                List<DetectedRejection> detected = rejectionDetectorService.detectRejections(sampleOfficeAction);
                RejectionAnalysis analysis = llmService.explainAndSuggest(sampleOfficeAction, detected, samplePatentDraft);

                model.addAttribute("officeActionText", sampleOfficeAction);
                model.addAttribute("patentDraftText", samplePatentDraft);
                model.addAttribute("analysis", analysis);
                model.addAttribute("isSample", true);

                analysisHistoryRepository.save(new AnalysisHistoryEntity(
                        "REJECTION",
                        "Office Action Rejection Explainer (Sample Demo)",
                        "Detected " + analysis.getTotalRejections() + " statutory rejections (101, 102, 103, 112) with targeted amendments."
                ));
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Could not load sample office action: " + e.getMessage());
            }
        }
        return "rejection";
    }

    @PostMapping("/rejection")
    public String analyzeOfficeAction(
            @RequestParam(name = "officeActionFile", required = false) MultipartFile officeActionFile,
            @RequestParam(name = "officeActionText", required = false) String officeActionText,
            @RequestParam(name = "patentDraftText", required = false) String patentDraftText,
            Model model) {

        try {
            String extractedOA = officeActionFile != null && !officeActionFile.isEmpty()
                    ? DocumentParserUtil.extractText(officeActionFile)
                    : officeActionText;

            if (extractedOA == null || extractedOA.isBlank()) {
                model.addAttribute("errorMessage", "Please upload an Office Action file or paste its text.");
                return "rejection";
            }

            List<DetectedRejection> detected = rejectionDetectorService.detectRejections(extractedOA);
            RejectionAnalysis analysis = llmService.explainAndSuggest(extractedOA, detected, patentDraftText);

            model.addAttribute("officeActionText", extractedOA);
            model.addAttribute("patentDraftText", patentDraftText);
            model.addAttribute("analysis", analysis);

            analysisHistoryRepository.save(new AnalysisHistoryEntity(
                    "REJECTION",
                    "Office Action Rejection Analysis",
                    "Extracted " + analysis.getTotalRejections() + " statutory grounds. AI prosecution recommendations generated."
            ));

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error analyzing office action: " + e.getMessage());
        }

        return "rejection";
    }

    private String loadClasspathSample(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
