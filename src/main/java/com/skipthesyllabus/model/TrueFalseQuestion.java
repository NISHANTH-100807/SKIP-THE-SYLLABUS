package com.skipthesyllabus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "true_false_questions")
public class TrueFalseQuestion extends Question {

    @Column(nullable = false)
    private Boolean correctAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    private Integer questionOrder;

    public TrueFalseQuestion() {}

    public TrueFalseQuestion(String questionText, Boolean correctAnswer) {
        this.setQuestionText(questionText);
        this.correctAnswer = correctAnswer;
    }

    @Override
    public boolean checkAnswer(String answer) {
        if (answer == null || correctAnswer == null) return false;
        boolean boolAnswer = "true".equalsIgnoreCase(answer.trim()) || "yes".equalsIgnoreCase(answer.trim());
        return correctAnswer == boolAnswer;
    }

    @Override
    public String getCorrectAnswer() { return correctAnswer != null ? correctAnswer.toString() : "false"; }

    @Override
    public String getQuestionType() { return "TRUE_FALSE"; }

    public Boolean getCorrectAnswerBool() { return correctAnswer; }
    public void setCorrectAnswer(Boolean correctAnswer) { this.correctAnswer = correctAnswer; }
    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
    public Integer getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(Integer questionOrder) { this.questionOrder = questionOrder; }
}
