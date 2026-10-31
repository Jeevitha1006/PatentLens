package com.patentlens.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility for parsing text from uploaded patent documents (PDF and TXT formats).
 */
public final class DocumentParserUtil {

    private DocumentParserUtil() {
        // Prevent instantiation
    }

    /**
     * Extracts text from an uploaded MultipartFile.
     * Detects PDF via file extension or content type, falling back to UTF-8 text parsing.
     *
     * @param file The uploaded file
     * @return Extracted plain text
     * @throws IOException If file reading or PDF parsing fails
     */
    public static String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return "";
        }
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        byte[] bytes = file.getBytes();

        if (filename.endsWith(".pdf") || isPdfMagicBytes(bytes)) {
            return extractTextFromPdf(bytes);
        } else {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * Extracts plain text from raw PDF bytes using Apache PDFBox.
     *
     * @param pdfBytes Raw bytes of the PDF
     * @return Extracted text
     * @throws IOException If PDF parsing fails
     */
    public static String extractTextFromPdf(byte[] pdfBytes) throws IOException {
        try (InputStream is = new ByteArrayInputStream(pdfBytes);
             PDDocument document = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    /**
     * Checks if the byte array starts with the '%PDF-' magic header.
     */
    private static boolean isPdfMagicBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return false;
        }
        return bytes[0] == 0x25 && bytes[1] == 0x50 && bytes[2] == 0x44 && bytes[3] == 0x46; // %PDF
    }
}
