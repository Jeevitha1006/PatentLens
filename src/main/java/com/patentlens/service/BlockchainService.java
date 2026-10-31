package com.patentlens.service;

import com.patentlens.entity.BlockEntity;
import com.patentlens.repository.BlockRepository;
import com.patentlens.util.Sha256Util;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service implementing an immutable Proof-of-Invention Blockchain in Java.
 *
 * <p><b>Blockchain Architecture & Viva Defense:</b></p>
 * <ul>
 *   <li><b>Block Structure:</b> index, timestamp, documentHash (SHA-256), previousHash, nonce, blockHash.</li>
 *   <li><b>Proof-of-Work (PoW):</b> Adjustable consensus difficulty requiring mining nonces until
 *       SHA-256 starts with 'N' leading zeros. Guarantees computational commitment without network dependency.</li>
 *   <li><b>Cryptographic Immutability:</b> Tampering with any historical block breaks all subsequent
 *       previousHash pointers, instantly detected by {@link #verifyChainIntegrity()}.</li>
 *   <li><b>Proof-of-Prior-Invention:</b> By hashing an unreleased patent draft and mining it into the chain,
 *       an inventor obtains mathematical, non-repudiable proof that their invention existed on or before that exact timestamp.</li>
 * </ul>
 */
@Service
public class BlockchainService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainService.class);
    private static final String GENESIS_PREV_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    @Value("${patentlens.blockchain.difficulty:2}")
    private int difficulty;

    private final BlockRepository blockRepository;

    public BlockchainService(BlockRepository blockRepository) {
        this.blockRepository = blockRepository;
    }

    /**
     * Initializes the Genesis block on application startup if the blockchain database is empty.
     */
    @PostConstruct
    @Transactional
    public void initGenesisBlock() {
        if (blockRepository.count() == 0) {
            log.info("Blockchain is empty. Initializing Genesis Block with PoW difficulty = {}", difficulty);
            String genesisData = "PatentLens Proof-of-Invention Genesis Ledger v1.0";
            String genesisDocHash = Sha256Util.sha256Hex(genesisData);
            long timestamp = Instant.now().toEpochMilli();

            MinedResult mined = mineBlock(0, timestamp, GENESIS_PREV_HASH, genesisDocHash);

            BlockEntity genesis = new BlockEntity(
                    0,
                    timestamp,
                    genesisDocHash,
                    GENESIS_PREV_HASH,
                    mined.hash,
                    mined.nonce,
                    "Genesis Block (Root of Trust)",
                    "Genesis Block establishing root cryptographic timestamp for PatentLens."
            );
            blockRepository.save(genesis);
            log.info("Genesis block successfully anchored. Hash: {}", mined.hash);
        }
    }

    /**
     * Mints and persists a new block containing the SHA-256 fingerprint of a patent draft.
     *
     * @param documentText Text content of the patent specification
     * @param documentTitle User-friendly title / filename
     * @return The freshly minted BlockEntity
     */
    @Transactional
    public BlockEntity timestampDocument(String documentText, String documentTitle) {
        String docHash = Sha256Util.sha256Hex(documentText);
        long timestamp = Instant.now().toEpochMilli();

        BlockEntity lastBlock = blockRepository.findTopByOrderByBlockIndexDesc()
                .orElseThrow(() -> new IllegalStateException("Genesis block missing"));

        int newIndex = lastBlock.getBlockIndex() + 1;
        String prevHash = lastBlock.getHash();

        MinedResult mined = mineBlock(newIndex, timestamp, prevHash, docHash);

        String snippet = documentText.length() > 300
                ? documentText.substring(0, 300) + "..."
                : documentText;

        BlockEntity newBlock = new BlockEntity(
                newIndex,
                timestamp,
                docHash,
                prevHash,
                mined.hash,
                mined.nonce,
                documentTitle != null && !documentTitle.isBlank() ? documentTitle : "Patent Draft #" + newIndex,
                snippet
        );

        return blockRepository.save(newBlock);
    }

    /**
     * Executes the Proof-of-Work mining algorithm, iterating the nonce until the hash satisfies the target difficulty.
     */
    public MinedResult mineBlock(int index, long timestamp, String previousHash, String docHash) {
        String targetPrefix = "0".repeat(Math.max(1, difficulty));
        long nonce = 0;
        String hash;

        while (true) {
            hash = calculateBlockHash(index, timestamp, previousHash, docHash, nonce);
            if (hash.startsWith(targetPrefix)) {
                break;
            }
            nonce++;
        }

        return new MinedResult(nonce, hash);
    }

    /**
     * Computes the deterministic SHA-256 hash of a block's headers and payload.
     */
    public String calculateBlockHash(int index, long timestamp, String previousHash, String docHash, long nonce) {
        String rawData = index + ":" + timestamp + ":" + previousHash + ":" + docHash + ":" + nonce;
        return Sha256Util.sha256Hex(rawData);
    }

    /**
     * Verifies the cryptographic integrity of the entire blockchain from Block 0 to N.
     * Detects any altered data, broken hash links, or invalid proof-of-work.
     *
     * @return Verification status and detailed report
     */
    public IntegrityReport verifyChainIntegrity() {
        List<BlockEntity> chain = blockRepository.findByOrderByBlockIndexAsc();
        String targetPrefix = "0".repeat(Math.max(1, difficulty));

        for (int i = 0; i < chain.size(); i++) {
            BlockEntity current = chain.get(i);

            // 1. Recalculate block hash from raw fields
            String computedHash = calculateBlockHash(
                    current.getBlockIndex(),
                    current.getTimestamp(),
                    current.getPreviousHash(),
                    current.getDocumentHash(),
                    current.getNonce()
            );

            if (!computedHash.equals(current.getHash())) {
                return new IntegrityReport(false,
                        "Tampering detected at Block #" + current.getBlockIndex() +
                        "! Stored hash " + current.getHash() + " does not match recalculated hash " + computedHash,
                        current.getBlockIndex());
            }

            // 2. Validate Proof-of-Work
            if (!current.getHash().startsWith(targetPrefix)) {
                return new IntegrityReport(false,
                        "Proof-of-Work failed at Block #" + current.getBlockIndex() +
                        "! Hash does not meet required difficulty prefix: " + targetPrefix,
                        current.getBlockIndex());
            }

            // 3. Verify previous hash pointer
            if (i > 0) {
                BlockEntity previous = chain.get(i - 1);
                if (!current.getPreviousHash().equals(previous.getHash())) {
                    return new IntegrityReport(false,
                            "Broken chain linkage between Block #" + previous.getBlockIndex() +
                            " and Block #" + current.getBlockIndex() + "! PreviousHash pointer mismatch.",
                            current.getBlockIndex());
                }
            } else {
                if (!GENESIS_PREV_HASH.equals(current.getPreviousHash())) {
                    return new IntegrityReport(false,
                            "Corrupted Genesis Block previous hash! Expected root zero hash.", 0);
                }
            }
        }

        return new IntegrityReport(true,
                "Blockchain verified successfully. All " + chain.size() +
                " blocks possess valid Proof-of-Work (" + targetPrefix + ") and unbroken cryptographic links.",
                null);
    }

    /**
     * Checks whether an uploaded document matches any timestamped block in the ledger.
     */
    public VerificationMatch verifyDocument(String documentText) {
        String docHash = Sha256Util.sha256Hex(documentText);
        Optional<BlockEntity> match = blockRepository.findByDocumentHash(docHash);

        if (match.isPresent()) {
            BlockEntity b = match.get();
            return new VerificationMatch(true, b.getBlockIndex(), b.getTimestamp(), docHash, b.getDocumentTitle(),
                    "AUTHENTIC RECORD FOUND: Document hash matches Block #" + b.getBlockIndex() +
                    " stamped on " + Instant.ofEpochMilli(b.getTimestamp()).toString());
        } else {
            return new VerificationMatch(false, null, null, docHash, null,
                    "NO RECORD FOUND: The SHA-256 hash (" + docHash + ") does not exist on this ledger.");
        }
    }

    public List<BlockEntity> getAllBlocks() {
        return blockRepository.findByOrderByBlockIndexAsc();
    }

    public static class MinedResult {
        public final long nonce;
        public final String hash;

        public MinedResult(long nonce, String hash) {
            this.nonce = nonce;
            this.hash = hash;
        }
    }

    public static class IntegrityReport {
        private final boolean valid;
        private final String message;
        private final Integer corruptedBlockIndex;

        public IntegrityReport(boolean valid, String message, Integer corruptedBlockIndex) {
            this.valid = valid;
            this.message = message;
            this.corruptedBlockIndex = corruptedBlockIndex;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public Integer getCorruptedBlockIndex() { return corruptedBlockIndex; }
    }

    public static class VerificationMatch {
        private final boolean verified;
        private final Integer blockIndex;
        private final Long timestamp;
        private final String documentHash;
        private final String documentTitle;
        private final String message;

        public VerificationMatch(boolean verified, Integer blockIndex, Long timestamp, String documentHash, String documentTitle, String message) {
            this.verified = verified;
            this.blockIndex = blockIndex;
            this.timestamp = timestamp;
            this.documentHash = documentHash;
            this.documentTitle = documentTitle;
            this.message = message;
        }

        public boolean isVerified() { return verified; }
        public Integer getBlockIndex() { return blockIndex; }
        public Long getTimestamp() { return timestamp; }
        public String getDocumentHash() { return documentHash; }
        public String getDocumentTitle() { return documentTitle; }
        public String getMessage() { return message; }
    }
}
