package com.skipthesyllabus.dto;

import java.time.LocalDateTime;

public record QuizAttemptSummaryDTO(
    Long attemptId,
    Long quizId,
    String documentName,
    Integer totalQuestions,
    Integer correctAnswers,
    Double scorePercent,
    LocalDateTime attemptedAt
) {}
