package com.skipthesyllabus.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private LocalDateTime attemptedAt = LocalDateTime.now();

    private Integer totalQuestions;
    private Integer correctAnswers;
    private Double scorePercent;

    @ElementCollection
    @CollectionTable(name = "attempt_answers", joinColumns = @JoinColumn(name = "attempt_id"))
    @MapKeyColumn(name = "question_id")
    @Column(name = "submitted_answer")
    private Map<Long, String> submittedAnswers = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "attempt_results", joinColumns = @JoinColumn(name = "attempt_id"))
    @MapKeyColumn(name = "question_id")
    @Column(name = "is_correct")
    private Map<Long, Boolean> answerResults = new HashMap<>();

    public QuizAttempt() {}

    public QuizAttempt(Quiz quiz) {
        this.quiz = quiz;
        this.totalQuestions = quiz.getQuestions().size();
    }

    public void computeScore() {
        long correct = answerResults.values().stream().filter(Boolean::booleanValue).count();
        this.correctAnswers = (int) correct;
        this.scorePercent = totalQuestions > 0 ? (correct * 100.0 / totalQuestions) : 0.0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
    public LocalDateTime getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(LocalDateTime attemptedAt) { this.attemptedAt = attemptedAt; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public Integer getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }
    public Double getScorePercent() { return scorePercent; }
    public void setScorePercent(Double scorePercent) { this.scorePercent = scorePercent; }
    public Map<Long, String> getSubmittedAnswers() { return submittedAnswers; }
    public void setSubmittedAnswers(Map<Long, String> submittedAnswers) { this.submittedAnswers = submittedAnswers; }
    public Map<Long, Boolean> getAnswerResults() { return answerResults; }
    public void setAnswerResults(Map<Long, Boolean> answerResults) { this.answerResults = answerResults; }
}
