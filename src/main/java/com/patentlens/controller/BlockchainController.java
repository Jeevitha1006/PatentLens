package com.patentlens.controller;

import com.patentlens.entity.AnalysisHistoryEntity;
import com.patentlens.entity.BlockEntity;
import com.patentlens.repository.AnalysisHistoryRepository;
import com.patentlens.service.BlockchainService;
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
public class BlockchainController {

    private final BlockchainService blockchainService;
    private final AnalysisHistoryRepository analysisHistoryRepository;

    public BlockchainController(BlockchainService blockchainService, AnalysisHistoryRepository analysisHistoryRepository) {
        this.blockchainService = blockchainService;
        this.analysisHistoryRepository = analysisHistoryRepository;
    }

    @GetMapping("/blockchain")
    public String showBlockchainPage(Model model) {
        List<BlockEntity> blocks = blockchainService.getAllBlocks();
        model.addAttribute("blocks", blocks);
        return "blockchain";
    }

    @PostMapping("/blockchain/timestamp")
    public String timestampDocument(
            @RequestParam(name = "documentFile", required = false) MultipartFile documentFile,
            @RequestParam(name = "documentText", required = false) String documentText,
            @RequestParam(name = "documentTitle", required = false) String documentTitle,
            @RequestParam(name = "useSample", required = false) Boolean useSample,
            Model model) {

        try {
            String textToStamp;
            String title = documentTitle;

            if (Boolean.TRUE.equals(useSample)) {
                textToStamp = loadClasspathSample("samples/patent_draft_new.txt");
                title = "Amended Autonomous UAV Patent Draft (Sample)";
            } else if (documentFile != null && !documentFile.isEmpty()) {
                textToStamp = DocumentParserUtil.extractText(documentFile);
                if (title == null || title.isBlank()) {
                    title = documentFile.getOriginalFilename();
                }
            } else {
                textToStamp = documentText;
            }

            if (textToStamp == null || textToStamp.isBlank()) {
                model.addAttribute("errorMessage", "Please upload a patent document or provide text to timestamp.");
            } else {
                BlockEntity newBlock = blockchainService.timestampDocument(textToStamp, title);
                model.addAttribute("successMessage", "Document successfully anchored to Blockchain! Mined Block #" +
                        newBlock.getBlockIndex() + " with SHA-256 Hash: " + newBlock.getHash());

                analysisHistoryRepository.save(new AnalysisHistoryEntity(
                        "BLOCKCHAIN",
                        "Proof-of-Invention Timestamp",
                        "Anchored '" + newBlock.getDocumentTitle() + "' in Block #" + newBlock.getBlockIndex() + "."
                ));
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error anchoring document: " + e.getMessage());
        }

        model.addAttribute("blocks", blockchainService.getAllBlocks());
        return "blockchain";
    }

    @PostMapping("/blockchain/verify-chain")
    public String verifyChain(Model model) {
        BlockchainService.IntegrityReport report = blockchainService.verifyChainIntegrity();
        model.addAttribute("integrityReport", report);
        model.addAttribute("blocks", blockchainService.getAllBlocks());

        analysisHistoryRepository.save(new AnalysisHistoryEntity(
                "BLOCKCHAIN",
                "Blockchain Cryptographic Audit",
                report.isValid() ? "Audit passed: All block hashes and links intact." : "Audit alert: " + report.getMessage()
        ));

        return "blockchain";
    }

    @PostMapping("/blockchain/verify-doc")
    public String verifyDocument(
            @RequestParam(name = "verifyFile", required = false) MultipartFile verifyFile,
            @RequestParam(name = "verifyText", required = false) String verifyText,
            @RequestParam(name = "useSample", required = false) Boolean useSample,
            Model model) {

        try {
            String textToVerify;
            if (Boolean.TRUE.equals(useSample)) {
                textToVerify = loadClasspathSample("samples/patent_draft_new.txt");
            } else if (verifyFile != null && !verifyFile.isEmpty()) {
                textToVerify = DocumentParserUtil.extractText(verifyFile);
            } else {
                textToVerify = verifyText;
            }

            if (textToVerify == null || textToVerify.isBlank()) {
                model.addAttribute("verifyErrorMessage", "Please upload or paste a document to verify its hash.");
            } else {
                BlockchainService.VerificationMatch match = blockchainService.verifyDocument(textToVerify);
                model.addAttribute("verificationMatch", match);
            }
        } catch (Exception e) {
            model.addAttribute("verifyErrorMessage", "Error verifying document: " + e.getMessage());
        }

        model.addAttribute("blocks", blockchainService.getAllBlocks());
        return "blockchain";
    }

    private String loadClasspathSample(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
