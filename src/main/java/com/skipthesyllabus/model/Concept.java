package com.skipthesyllabus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "concepts")
public class Concept {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "CLOB")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private PDFDocument document;

    private Integer incorrectCount = 0;
    private Integer attemptCount = 0;

    public Concept() {}

    public Concept(String name, PDFDocument document) {
        this.name = name;
        this.document = document;
    }

    public void recordAttempt(boolean correct) {
        this.attemptCount++;
        if (!correct) this.incorrectCount++;
    }

    public double getErrorRate() {
        return attemptCount > 0 ? (double) incorrectCount / attemptCount : 0.0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public PDFDocument getDocument() { return document; }
    public void setDocument(PDFDocument document) { this.document = document; }
    public Integer getIncorrectCount() { return incorrectCount; }
    public void setIncorrectCount(Integer incorrectCount) { this.incorrectCount = incorrectCount; }
    public Integer getAttemptCount() { return attemptCount; }
    public void setAttemptCount(Integer attemptCount) { this.attemptCount = attemptCount; }
}
