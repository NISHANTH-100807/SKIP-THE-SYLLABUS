package com.skipthesyllabus.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class QuizRequestDTO {
    @Min(value = 1, message = "Minimum 1 question")
    @Max(value = 20, message = "Maximum 20 questions")
    private Integer questionCount = 5;

    @NotBlank
    private String difficulty = "mixed";

    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}
