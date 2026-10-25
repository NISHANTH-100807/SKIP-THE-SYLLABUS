package com.skipthesyllabus.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private PDFDocument document;

    private String difficulty;
    private Integer questionCount;

    @Enumerated(EnumType.STRING)
    private QuizSource source = QuizSource.AI_GENERATED;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MCQQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizAttempt> attempts = new ArrayList<>();

    public enum QuizSource { AI_GENERATED, LOCAL_FALLBACK }

    public Quiz() {}

    public Quiz(PDFDocument document, String difficulty, Integer questionCount) {
        this.document = document;
        this.difficulty = difficulty;
        this.questionCount = questionCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PDFDocument getDocument() { return document; }
    public void setDocument(PDFDocument document) { this.document = document; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }
    public QuizSource getSource() { return source; }
    public void setSource(QuizSource source) { this.source = source; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<MCQQuestion> getQuestions() { return questions; }
    public void setQuestions(List<MCQQuestion> questions) { this.questions = questions; }
    public List<QuizAttempt> getAttempts() { return attempts; }
    public void setAttempts(List<QuizAttempt> attempts) { this.attempts = attempts; }
}
