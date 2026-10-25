package com.skipthesyllabus.model;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "CLOB", nullable = false)
    private String questionText;

    @Column(columnDefinition = "CLOB")
    private String explanation;

    private String difficulty;
    private Integer pageReference;

    public abstract boolean checkAnswer(String answer);
    public abstract String getCorrectAnswer();
    public abstract String getQuestionType();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Integer getPageReference() { return pageReference; }
    public void setPageReference(Integer pageReference) { this.pageReference = pageReference; }
}
