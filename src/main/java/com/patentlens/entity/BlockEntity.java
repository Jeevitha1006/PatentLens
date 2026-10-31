package com.patentlens.entity;

import jakarta.persistence.*;

/**
 * JPA Entity representing a Block in the Proof-of-Invention Blockchain.
 * Stored persistently in the H2 database.
 */
@Entity
@Table(name = "patent_blockchain")
public class BlockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "block_index", nullable = false)
    private int blockIndex;

    @Column(nullable = false)
    private long timestamp;

    @Column(name = "document_hash", nullable = false, length = 64)
    private String documentHash;

    @Column(name = "previous_hash", nullable = false, length = 64)
    private String previousHash;

    @Column(name = "hash", nullable = false, length = 64)
    private String hash;

    @Column(nullable = false)
    private long nonce;

    @Column(name = "document_title", length = 255)
    private String documentTitle;

    @Column(name = "document_snippet", length = 2000)
    private String documentSnippet;

    public BlockEntity() {
    }

    public BlockEntity(int blockIndex, long timestamp, String documentHash, String previousHash,
                       String hash, long nonce, String documentTitle, String documentSnippet) {
        this.blockIndex = blockIndex;
        this.timestamp = timestamp;
        this.documentHash = documentHash;
        this.previousHash = previousHash;
        this.hash = hash;
        this.nonce = nonce;
        this.documentTitle = documentTitle;
        this.documentSnippet = documentSnippet;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getBlockIndex() {
        return blockIndex;
    }

    public void setBlockIndex(int blockIndex) {
        this.blockIndex = blockIndex;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDocumentHash() {
        return documentHash;
    }

    public void setDocumentHash(String documentHash) {
        this.documentHash = documentHash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getDocumentSnippet() {
        return documentSnippet;
    }

    public void setDocumentSnippet(String documentSnippet) {
        this.documentSnippet = documentSnippet;
    }
}
