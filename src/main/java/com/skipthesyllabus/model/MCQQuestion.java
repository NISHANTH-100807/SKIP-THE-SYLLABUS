package com.skipthesyllabus.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "mcq_questions")
public class MCQQuestion extends Question {

    @ElementCollection
    @CollectionTable(name = "mcq_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text", columnDefinition = "CLOB")
    private List<String> options;

    @Column(nullable = false)
    private String correctAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    private Integer questionOrder;

    public MCQQuestion() {}

    public MCQQuestion(String questionText, List<String> options, String correctAnswer) {
        this.setQuestionText(questionText);
        this.options = options != null ? new java.util.ArrayList<>(options) : new java.util.ArrayList<>();
        this.correctAnswer = correctAnswer;
    }

    @Override
    public boolean checkAnswer(String answer) {
        if (answer == null || correctAnswer == null) return false;
        return correctAnswer.trim().equalsIgnoreCase(answer.trim());
    }

    @Override
    public String getCorrectAnswer() { return correctAnswer; }

    @Override
    public String getQuestionType() { return "MCQ"; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) {
        this.options = options != null ? new java.util.ArrayList<>(options) : new java.util.ArrayList<>();
    }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
    public Integer getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(Integer questionOrder) { this.questionOrder = questionOrder; }
}
