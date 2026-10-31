package com.patentlens.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for tracking recent patent analysis activity displayed on the Dashboard.
 */
@Entity
@Table(name = "analysis_history")
public class AnalysisHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analysis_type", nullable = false, length = 50)
    private String analysisType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 2000)
    private String summary;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AnalysisHistoryEntity() {
    }

    public AnalysisHistoryEntity(String analysisType, String title, String summary) {
        this.analysisType = analysisType;
        this.title = title;
        this.summary = summary;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
