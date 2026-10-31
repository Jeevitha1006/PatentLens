package com.patentlens.controller;

import com.patentlens.entity.AnalysisHistoryEntity;
import com.patentlens.model.ClaimDiffResult;
import com.patentlens.repository.AnalysisHistoryRepository;
import com.patentlens.service.ClaimDiffService;
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
public class CompareController {

    private final ClaimDiffService claimDiffService;
    private final AnalysisHistoryRepository analysisHistoryRepository;

    public CompareController(ClaimDiffService claimDiffService, AnalysisHistoryRepository analysisHistoryRepository) {
        this.claimDiffService = claimDiffService;
        this.analysisHistoryRepository = analysisHistoryRepository;
    }

    @GetMapping("/compare")
    public String showComparePage(@RequestParam(name = "sample", required = false) Boolean sample, Model model) {
        if (Boolean.TRUE.equals(sample)) {
            try {
                String oldText = loadClasspathSample("samples/patent_draft_old.txt");
                String newText = loadClasspathSample("samples/patent_draft_new.txt");
                ClaimDiffResult result = claimDiffService.comparePatentDrafts(oldText, newText);

                model.addAttribute("oldText", oldText);
                model.addAttribute("newText", newText);
                model.addAttribute("diffResult", result);
                model.addAttribute("isSample", true);

                analysisHistoryRepository.save(new AnalysisHistoryEntity(
                        "COMPARE",
                        "Patent Claim Comparison (Demo Sample)",
                        "Evaluated claims: " + result.getModifiedCount() + " modified, " +
                        result.getAddedCount() + " added, " + result.getRemovedCount() + " removed."
                ));
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Could not load sample draft: " + e.getMessage());
            }
        }
        return "compare";
    }

    @PostMapping("/compare")
    public String compareDrafts(
            @RequestParam(name = "oldFile", required = false) MultipartFile oldFile,
            @RequestParam(name = "newFile", required = false) MultipartFile newFile,
            @RequestParam(name = "oldText", required = false) String oldText,
            @RequestParam(name = "newText", required = false) String newText,
            Model model) {

        try {
            String extractedOld = oldFile != null && !oldFile.isEmpty() ? DocumentParserUtil.extractText(oldFile) : oldText;
            String extractedNew = newFile != null && !newFile.isEmpty() ? DocumentParserUtil.extractText(newFile) : newText;

            if ((extractedOld == null || extractedOld.isBlank()) && (extractedNew == null || extractedNew.isBlank())) {
                model.addAttribute("errorMessage", "Please upload patent files or paste draft text to compare.");
                return "compare";
            }

            ClaimDiffResult result = claimDiffService.comparePatentDrafts(extractedOld, extractedNew);

            model.addAttribute("oldText", extractedOld);
            model.addAttribute("newText", extractedNew);
            model.addAttribute("diffResult", result);

            analysisHistoryRepository.save(new AnalysisHistoryEntity(
                    "COMPARE",
                    "Patent Claim Comparison",
                    "Analyzed " + result.getTotalNewClaims() + " claims. Result: " +
                    result.getModifiedCount() + " modified, " + result.getAddedCount() + " added."
            ));

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error parsing drafts: " + e.getMessage());
        }

        return "compare";
    }

    private String loadClasspathSample(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
