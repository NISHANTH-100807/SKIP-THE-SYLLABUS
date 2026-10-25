package com.skipthesyllabus.repository;

import com.skipthesyllabus.model.PDFDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PDFDocumentRepository extends JpaRepository<PDFDocument, Long> {
    List<PDFDocument> findAllByOrderByUploadedAtDesc();
    List<PDFDocument> findByOriginalFilenameContainingIgnoreCaseOrderByUploadedAtDesc(String query);
    long countByStatus(PDFDocument.ProcessingStatus status);
}
