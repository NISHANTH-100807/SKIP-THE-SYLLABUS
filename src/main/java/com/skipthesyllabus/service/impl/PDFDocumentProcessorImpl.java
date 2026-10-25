package com.skipthesyllabus.service.impl;

import com.skipthesyllabus.exception.InvalidPDFException;
import com.skipthesyllabus.model.DocumentSection;
import com.skipthesyllabus.model.PDFDocument;
import com.skipthesyllabus.repository.DocumentSectionRepository;
import com.skipthesyllabus.repository.PDFDocumentRepository;
import com.skipthesyllabus.service.DocumentProcessor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ENCAPSULATION: PDF handling internals hidden behind DocumentProcessor interface.
 * Validates, persists, and asynchronously extracts text from uploaded PDFs.
 */
@Service
public class PDFDocumentProcessorImpl implements DocumentProcessor {

    private static final Logger log = LoggerFactory.getLogger(PDFDocumentProcessorImpl.class);
    private static final String PDF_MAGIC = "%PDF";

    @Value("${app.upload.max-size-bytes:52428800}")
    private long maxSizeBytes;

    private final PDFDocumentRepository docRepo;
    private final DocumentSectionRepository sectionRepo;

    public PDFDocumentProcessorImpl(PDFDocumentRepository docRepo,
                                    DocumentSectionRepository sectionRepo) {
        this.docRepo = docRepo;
        this.sectionRepo = sectionRepo;
    }

    // -------------------------------------------------------------------------
    // DocumentProcessor interface
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public PDFDocument process(MultipartFile file) {
        validateFile(file);

        String sanitized = sanitizeFilename(file.getOriginalFilename());
        PDFDocument doc = new PDFDocument(file.getOriginalFilename(), sanitized, file.getSize());
        doc = docRepo.save(doc);

        try {
            byte[] bytes = file.getBytes();
            extractAndStore(doc, bytes);
        } catch (IOException e) {
            log.error("Failed to read uploaded file bytes: {}", e.getMessage());
            doc.markAsFailed();
            docRepo.save(doc);
        }

        return docRepo.findById(doc.getId()).orElse(doc);
    }

    @Override
    @Transactional
    public void extractAndStore(PDFDocument document, byte[] fileBytes) {
        document.markAsProcessing();
        docRepo.save(document);

        try (PDDocument pdf = Loader.loadPDF(fileBytes)) {
            int pageCount = pdf.getNumberOfPages();
            StringBuilder fullText = new StringBuilder();
            List<DocumentSection> sections = new ArrayList<>();

            PDFTextStripper stripper = new PDFTextStripper();

            for (int page = 1; page <= pageCount; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = stripper.getText(pdf).trim();
                fullText.append(pageText).append("\n");

                if (!pageText.isBlank()) {
                    // Split each page into ~800-word sections
                    List<String> chunks = splitIntoChunks(pageText, 800);
                    for (int i = 0; i < chunks.size(); i++) {
                        DocumentSection section = new DocumentSection(document, page, i, chunks.get(i));
                        sections.add(section);
                    }
                }
            }

            // Delete old sections if re-processing
            sectionRepo.deleteByDocumentId(document.getId());
            sectionRepo.saveAll(sections);

            document.markAsReady(fullText.toString(), pageCount);
            docRepo.save(document);
            log.info("Extracted {} pages, {} sections from document {}", pageCount, sections.size(), document.getId());

        } catch (Exception e) {
            log.error("PDF extraction failed for document {}: {}", document.getId(), e.getMessage());
            document.markAsFailed();
            docRepo.save(document);
        }
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidPDFException("No file provided or file is empty.");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidPDFException("File size exceeds maximum allowed size of 50MB.");
        }

        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".pdf")) {
            throw new InvalidPDFException("Only PDF files are accepted.");
        }

        // Check PDF magic bytes
        try {
            byte[] header = new byte[4];
            int read = file.getInputStream().read(header);
            if (read < 4 || !new String(header).equals(PDF_MAGIC)) {
                throw new InvalidPDFException("File does not appear to be a valid PDF.");
            }
        } catch (IOException e) {
            throw new InvalidPDFException("Could not read file: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    private String sanitizeFilename(String original) {
        if (original == null) return "document.pdf";
        return original.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase();
    }

    private List<String> splitIntoChunks(String text, int wordsPerChunk) {
        String[] words = text.split("\\s+");
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int count = 0;
        for (String word : words) {
            current.append(word).append(' ');
            count++;
            if (count >= wordsPerChunk) {
                chunks.add(current.toString().trim());
                current = new StringBuilder();
                count = 0;
            }
        }
        if (!current.toString().isBlank()) {
            chunks.add(current.toString().trim());
        }
        return chunks.isEmpty() ? List.of(text) : chunks;
    }
}
