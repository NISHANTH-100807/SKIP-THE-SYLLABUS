package com.skipthesyllabus.dto;

import com.skipthesyllabus.model.PDFDocument;
import java.time.LocalDateTime;

public class DocumentDTO {
    private Long id;
    private String originalFilename;
    private Long fileSize;
    private Integer pageCount;
    private String status;
    private LocalDateTime uploadedAt;
    private LocalDateTime processedAt;
    private boolean hasText;

    public static DocumentDTO from(PDFDocument doc) {
        DocumentDTO dto = new DocumentDTO();
        dto.id = doc.getId();
        dto.originalFilename = doc.getOriginalFilename();
        dto.fileSize = doc.getFileSize();
        dto.pageCount = doc.getPageCount();
        dto.status = doc.getStatus().name();
        dto.uploadedAt = doc.getUploadedAt();
        dto.processedAt = doc.getProcessedAt();
        dto.hasText = doc.getExtractedText() != null && !doc.getExtractedText().isBlank();
        return dto;
    }

    public Long getId() { return id; }
    public String getOriginalFilename() { return originalFilename; }
    public Long getFileSize() { return fileSize; }
    public Integer getPageCount() { return pageCount; }
    public String getStatus() { return status; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public boolean isHasText() { return hasText; }
}
