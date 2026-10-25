package com.skipthesyllabus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pdf_documents")
public class PDFDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String sanitizedFilename;

    @Column(nullable = false)
    private Long fileSize;

    private Integer pageCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus status = ProcessingStatus.UPLOADED;

    @Column(columnDefinition = "CLOB")
    private String extractedText;

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    private LocalDateTime processedAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentSection> sections = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quiz> quizzes = new ArrayList<>();

    public enum ProcessingStatus {
        UPLOADED, PROCESSING, READY, FAILED, EMPTY_TEXT
    }

    public PDFDocument() {}

    public PDFDocument(String originalFilename, String sanitizedFilename, Long fileSize) {
        this.originalFilename = originalFilename;
        this.sanitizedFilename = sanitizedFilename;
        this.fileSize = fileSize;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getSanitizedFilename() { return sanitizedFilename; }
    public void setSanitizedFilename(String sanitizedFilename) { this.sanitizedFilename = sanitizedFilename; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    public ProcessingStatus getStatus() { return status; }
    public void setStatus(ProcessingStatus status) { this.status = status; }
    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public List<DocumentSection> getSections() { return sections; }
    public void setSections(List<DocumentSection> sections) { this.sections = sections; }
    public List<Quiz> getQuizzes() { return quizzes; }
    public void setQuizzes(List<Quiz> quizzes) { this.quizzes = quizzes; }

    public void markAsProcessing() { this.status = ProcessingStatus.PROCESSING; }

    public void markAsReady(String text, int pageCount) {
        this.extractedText = text;
        this.pageCount = pageCount;
        this.status = text == null || text.trim().isEmpty() ? ProcessingStatus.EMPTY_TEXT : ProcessingStatus.READY;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = ProcessingStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }

    public boolean isReady() { return status == ProcessingStatus.READY; }
}
