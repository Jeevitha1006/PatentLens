package com.patentlens.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.patentlens.model.DetectedRejection;
import com.patentlens.model.RejectionAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * Live HTTP client implementation of LlmService.
 * Calls an OpenAI-compatible REST API endpoint using standard Java HttpClient.
 * Automatically falls back to MockLlmService if no API key is provided or if network error occurs.
 */
@Service
@Primary
public class ApiLlmService implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(ApiLlmService.class);

    @Value("${llm.api.key:}")
    private String apiKey;

    @Value("${llm.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${llm.model:gpt-3.5-turbo}")
    private String modelName;

    private final MockLlmService mockLlmService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiLlmService(MockLlmService mockLlmService, ObjectMapper objectMapper) {
        this.mockLlmService = mockLlmService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public RejectionAnalysis explainAndSuggest(String officeActionText, List<DetectedRejection> detectedRejections, String patentDraftText) {
        // If API key is not configured or blank, fallback to high-quality mock
        if (apiKey == null || apiKey.trim().isEmpty() || "your_api_key_here".equalsIgnoreCase(apiKey.trim())) {
            log.info("No LLM API key configured. Utilizing MockLlmService for deterministic demonstration.");
            return mockLlmService.explainAndSuggest(officeActionText, detectedRejections, patentDraftText);
        }

        try {
            log.info("Calling live LLM API at {} with model {}", apiUrl, modelName);
            String prompt = buildPrompt(officeActionText, detectedRejections, patentDraftText);

            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("model", modelName);
            requestPayload.put("temperature", 0.3);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "system",
                    "content", "You are an expert USPTO patent attorney. Analyze patent office actions and provide plain-language explanations, amended claims, and legal overcoming arguments under 35 USC 101, 102, 103, 112."
            ));
            messages.add(Map.of("role", "user", "content", prompt));
            requestPayload.put("messages", messages);

            String requestBody = objectMapper.writeValueAsString(requestPayload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                String completion = root.path("choices").path(0).path("message").path("content").asText();
                return parseLlmResponse(completion, detectedRejections);
            } else {
                log.warn("LLM API returned non-200 status {}: {}. Falling back to mock.", response.statusCode(), response.body());
                return mockLlmService.explainAndSuggest(officeActionText, detectedRejections, patentDraftText);
            }
        } catch (Exception e) {
            log.warn("Failed to contact LLM API ({}). Gracefully falling back to MockLlmService.", e.getMessage());
            return mockLlmService.explainAndSuggest(officeActionText, detectedRejections, patentDraftText);
        }
    }

    private String buildPrompt(String officeActionText, List<DetectedRejection> rejections, String patentDraftText) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyze the following Patent Office Action and Claims:\n\n");
        sb.append("--- OFFICE ACTION ---\n").append(officeActionText).append("\n\n");
        if (patentDraftText != null && !patentDraftText.trim().isEmpty()) {
            sb.append("--- PATENT CLAIMS ---\n").append(patentDraftText).append("\n\n");
        }
        sb.append("For each detected rejection, provide:\n");
        sb.append("1. PLAIN LANGUAGE REASON (Why the examiner rejected it)\n");
        sb.append("2. SUGGESTED AMENDMENT (How to amend the claim to overcome the rejection)\n");
        sb.append("3. OVERCOMING ARGUMENT (Why the amendment overcomes the statutory ground)\n");
        return sb.toString();
    }

    private RejectionAnalysis parseLlmResponse(String completion, List<DetectedRejection> detectedRejections) {
        RejectionAnalysis analysis = new RejectionAnalysis();
        analysis.setUsedMock(false);
        analysis.setOverallStrategy("AI-Generated Prosecution Strategy:\n\n" + completion);

        // Assign response to rejections or keep fallback if specific parsing is partial
        for (DetectedRejection rej : detectedRejections) {
            rej.setPlainReason("Evaluated by " + modelName + " API.");
            rej.setSuggestedAmendment("Refer to comprehensive strategy generated above.");
            rej.setOvercomingArgument("Amendment addresses statutory boundaries identified in office action.");
        }
        analysis.setDetectedRejections(detectedRejections);
        return analysis;
    }
}
