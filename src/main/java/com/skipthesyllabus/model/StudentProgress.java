package com.skipthesyllabus.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "student_progress")
public class StudentProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date = LocalDate.now();
    private Integer documentsUploaded = 0;
    private Integer quizzesAttempted = 0;
    private Double averageScore = 0.0;
    private Integer explainSessions = 0;
    private Integer chatSessions = 0;

    public StudentProgress() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Integer getDocumentsUploaded() { return documentsUploaded; }
    public void setDocumentsUploaded(Integer documentsUploaded) { this.documentsUploaded = documentsUploaded; }
    public Integer getQuizzesAttempted() { return quizzesAttempted; }
    public void setQuizzesAttempted(Integer quizzesAttempted) { this.quizzesAttempted = quizzesAttempted; }
    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
    public Integer getExplainSessions() { return explainSessions; }
    public void setExplainSessions(Integer explainSessions) { this.explainSessions = explainSessions; }
    public Integer getChatSessions() { return chatSessions; }
    public void setChatSessions(Integer chatSessions) { this.chatSessions = chatSessions; }
}
