package com.patentlens.service;

import com.patentlens.model.DetectedRejection;
import com.patentlens.model.RejectionAnalysis;

import java.util.List;

/**
 * Interface defining LLM-powered patent rejection explanation and claim amendment generation.
 */
public interface LlmService {

    /**
     * Explains detected rejections in plain language and generates targeted claim amendments.
     *
     * @param officeActionText Full examiner office action text
     * @param detectedRejections List of rejections extracted by the rule engine
     * @param patentDraftText Patent specification / claim text
     * @return RejectionAnalysis containing structured explanations, amendments, and strategies
     */
    RejectionAnalysis explainAndSuggest(String officeActionText, List<DetectedRejection> detectedRejections, String patentDraftText);
}
