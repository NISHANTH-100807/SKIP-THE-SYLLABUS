package com.skipthesyllabus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "document_sections")
public class DocumentSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private PDFDocument document;

    private Integer pageNumber;
    private Integer sectionIndex;

    @Column(columnDefinition = "CLOB")
    private String content;

    private String title;

    @Column(name = "word_count")
    private Integer wordCount;

    public DocumentSection() {}

    public DocumentSection(PDFDocument document, Integer pageNumber, Integer sectionIndex, String content) {
        this.document = document;
        this.pageNumber = pageNumber;
        this.sectionIndex = sectionIndex;
        this.content = content;
        this.wordCount = content != null ? content.split("\\s+").length : 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PDFDocument getDocument() { return document; }
    public void setDocument(PDFDocument document) { this.document = document; }
    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }
    public Integer getSectionIndex() { return sectionIndex; }
    public void setSectionIndex(Integer sectionIndex) { this.sectionIndex = sectionIndex; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getWordCount() { return wordCount; }
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }
}
