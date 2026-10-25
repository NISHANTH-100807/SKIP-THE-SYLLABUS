package com.skipthesyllabus.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_sessions")
public class LearningSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private PDFDocument document;

    @Enumerated(EnumType.STRING)
    private SessionType sessionType;

    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime endedAt;

    @Column(columnDefinition = "CLOB")
    private String notes;

    public enum SessionType { EXPLAIN, QUIZ, CHAT, EXPLORE_3D }

    public LearningSession() {}

    public LearningSession(PDFDocument document, SessionType sessionType) {
        this.document = document;
        this.sessionType = sessionType;
    }

    public void end() { this.endedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PDFDocument getDocument() { return document; }
    public void setDocument(PDFDocument document) { this.document = document; }
    public SessionType getSessionType() { return sessionType; }
    public void setSessionType(SessionType sessionType) { this.sessionType = sessionType; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
