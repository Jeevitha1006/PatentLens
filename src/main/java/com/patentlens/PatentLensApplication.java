package com.patentlens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PatentLens: AI-Powered Patent Rejection Analyzer
 *
 * <p>Full-stack web application integrating:
 * <ul>
 *   <li>Patent Claim Diffing & Extraction</li>
 *   <li>USPTO Rejection Detection (35 USC 101, 102, 103, 112)</li>
 *   <li>LLM Rejection Explanations & Claim Amendment Generator</li>
 *   <li>TF-IDF & Cosine Similarity Novelty Heatmap</li>
 *   <li>Proof-of-Invention SHA-256 Blockchain with Proof-of-Work</li>
 *   <li>Scholarly Prior-Art Discovery (Semantic Scholar & OpenAlex)</li>
 *   <li>Stylometry Heuristics & AI-likeness Risk Indicator</li>
 * </ul>
 */
@SpringBootApplication
public class PatentLensApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatentLensApplication.class, args);
    }
}
