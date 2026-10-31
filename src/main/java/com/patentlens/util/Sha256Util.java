package com.patentlens.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for SHA-256 cryptographic hashing.
 * Used for hashing patent document contents and generating blockchain block hashes.
 */
public final class Sha256Util {

    private Sha256Util() {
        // Prevent instantiation
    }

    /**
     * Computes the SHA-256 hash of a string input and returns it as a lowercase hex string.
     *
     * @param input Raw text string
     * @return 64-character hexadecimal SHA-256 hash
     */
    public static String sha256Hex(String input) {
        if (input == null) {
            input = "";
        }
        return sha256Hex(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Computes the SHA-256 hash of raw byte data.
     *
     * @param data Byte array of input
     * @return 64-character hexadecimal SHA-256 hash
     */
    public static String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available on current JVM", e);
        }
    }
}
