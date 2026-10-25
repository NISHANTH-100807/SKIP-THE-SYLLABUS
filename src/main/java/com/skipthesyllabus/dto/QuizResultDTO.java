package com.skipthesyllabus.dto;

import java.util.List;

public class QuizResultDTO {
    private Long attemptId;
    private Long quizId;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Double scorePercent;
    private String scoreMessage;
    private List<QuestionResultDTO> questionResults;

    public record QuestionResultDTO(
        Long questionId,
        String questionText,
        String submittedAnswer,
        String correctAnswer,
        boolean isCorrect,
        String explanation,
        List<String> options
    ) {}

    public Long getAttemptId() { return attemptId; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }
    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public Integer getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }
    public Double getScorePercent() { return scorePercent; }
    public void setScorePercent(Double scorePercent) { this.scorePercent = scorePercent; }
    public String getScoreMessage() { return scoreMessage; }
    public void setScoreMessage(String scoreMessage) { this.scoreMessage = scoreMessage; }
    public List<QuestionResultDTO> getQuestionResults() { return questionResults; }
    public void setQuestionResults(List<QuestionResultDTO> questionResults) { this.questionResults = questionResults; }
}
