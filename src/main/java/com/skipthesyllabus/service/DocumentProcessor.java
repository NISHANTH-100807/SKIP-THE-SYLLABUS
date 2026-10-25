package com.skipthesyllabus.service;

import com.skipthesyllabus.model.PDFDocument;
import org.springframework.web.multipart.MultipartFile;

// ABSTRACTION
public interface DocumentProcessor {
    PDFDocument process(MultipartFile file);
    void extractAndStore(PDFDocument document, byte[] fileBytes);
}
