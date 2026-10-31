package com.patentlens.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of querying prior-art search engines with keywords extracted from patent claims.
 */
public class PriorArtSearchResult {

    private List<String> keywordsUsed = new ArrayList<>();
    private List<PriorArtArticle> articles = new ArrayList<>();
    private String statusMessage;
    private boolean success;

    public PriorArtSearchResult() {
    }

    public List<String> getKeywordsUsed() {
        return keywordsUsed;
    }

    public void setKeywordsUsed(List<String> keywordsUsed) {
        this.keywordsUsed = keywordsUsed;
    }

    public List<PriorArtArticle> getArticles() {
        return articles;
    }

    public void setArticles(List<PriorArtArticle> articles) {
        this.articles = articles;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
