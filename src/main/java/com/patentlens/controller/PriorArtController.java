package com.patentlens.controller;

import com.patentlens.entity.AnalysisHistoryEntity;
import com.patentlens.model.PriorArtSearchResult;
import com.patentlens.repository.AnalysisHistoryRepository;
import com.patentlens.service.PriorArtSearchService;
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
public class PriorArtController {

    private final PriorArtSearchService priorArtSearchService;
    private final AnalysisHistoryRepository analysisHistoryRepository;

    public PriorArtController(PriorArtSearchService priorArtSearchService, AnalysisHistoryRepository analysisHistoryRepository) {
        this.priorArtSearchService = priorArtSearchService;
        this.analysisHistoryRepository = analysisHistoryRepository;
    }

    @GetMapping("/prior-art")
    public String showPriorArtPage(@RequestParam(name = "sample", required = false) Boolean sample, Model model) {
        if (Boolean.TRUE.equals(sample)) {
            try {
                String sampleClaims = loadClasspathSample("samples/patent_draft_new.txt");
                PriorArtSearchResult result = priorArtSearchService.searchPriorArt(sampleClaims);

                model.addAttribute("claimsText", sampleClaims);
                model.addAttribute("searchResult", result);
                model.addAttribute("isSample", true);

                analysisHistoryRepository.save(new AnalysisHistoryEntity(
                        "PRIOR_ART",
                        "Prior-Art Scholarly Search (Sample Demo)",
                        "Queried literature with keywords: " + String.join(", ", result.getKeywordsUsed()) +
                        ". Discovered " + result.getArticles().size() + " citations."
                ));
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Could not load sample claims: " + e.getMessage());
            }
        }
        return "prior-art";
    }

    @PostMapping("/prior-art")
    public String searchPriorArt(
            @RequestParam(name = "claimsFile", required = false) MultipartFile claimsFile,
            @RequestParam(name = "claimsText", required = false) String claimsText,
            Model model) {

        try {
            String extracted = claimsFile != null && !claimsFile.isEmpty()
                    ? DocumentParserUtil.extractText(claimsFile)
                    : claimsText;

            if (extracted == null || extracted.isBlank()) {
                model.addAttribute("errorMessage", "Please provide patent claims text or upload a document to extract keywords.");
                return "prior-art";
            }

            PriorArtSearchResult result = priorArtSearchService.searchPriorArt(extracted);

            model.addAttribute("claimsText", extracted);
            model.addAttribute("searchResult", result);

            analysisHistoryRepository.save(new AnalysisHistoryEntity(
                    "PRIOR_ART",
                    "Scholarly Prior-Art Search",
                    "Extracted keywords: " + String.join(", ", result.getKeywordsUsed()) +
                    ". Found " + result.getArticles().size() + " publications."
            ));

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error searching prior art: " + e.getMessage());
        }

        return "prior-art";
    }

    private String loadClasspathSample(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
