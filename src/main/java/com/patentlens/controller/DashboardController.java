package com.patentlens.controller;

import com.patentlens.entity.AnalysisHistoryEntity;
import com.patentlens.repository.AnalysisHistoryRepository;
import com.patentlens.repository.BlockRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final BlockRepository blockRepository;
    private final AnalysisHistoryRepository analysisHistoryRepository;

    public DashboardController(BlockRepository blockRepository, AnalysisHistoryRepository analysisHistoryRepository) {
        this.blockRepository = blockRepository;
        this.analysisHistoryRepository = analysisHistoryRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        long blockCount = blockRepository.count();
        long analysisCount = analysisHistoryRepository.count();
        List<AnalysisHistoryEntity> recentActivities = analysisHistoryRepository.findTop10ByOrderByCreatedAtDesc();

        model.addAttribute("blockCount", blockCount);
        model.addAttribute("analysisCount", analysisCount);
        model.addAttribute("recentActivities", recentActivities);

        return "index";
    }
}
