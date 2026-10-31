package com.patentlens.model;

/**
 * Model representing a scholarly or patent reference retrieved from Semantic Scholar / OpenAlex.
 */
public class PriorArtArticle {

    private String title;
    private String authors;
    private String year;
    private String abstractSnippet;
    private String url;
    private String source;

    public PriorArtArticle() {
    }

    public PriorArtArticle(String title, String authors, String year, String abstractSnippet, String url, String source) {
        this.title = title;
        this.authors = authors;
        this.year = year;
        this.abstractSnippet = abstractSnippet;
        this.url = url;
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getAbstractSnippet() {
        return abstractSnippet;
    }

    public void setAbstractSnippet(String abstractSnippet) {
        this.abstractSnippet = abstractSnippet;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
