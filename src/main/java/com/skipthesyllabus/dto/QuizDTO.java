package com.skipthesyllabus.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skipthesyllabus.model.Quiz;
import java.time.LocalDateTime;
import java.util.List;

public class QuizDTO {
    private Long id;
    private Long documentId;
    private String documentName;
    private String difficulty;
    private Integer questionCount;
    private String source;
    private LocalDateTime createdAt;
    private List<QuestionDTO> questions;

    public record QuestionDTO(
        Long id,
        String questionText,
        List<String> options,
        String difficulty,
        Integer pageReference,
        Integer questionOrder,
        @JsonIgnore
        String correctAnswer,
        @JsonIgnore
        String explanation
    ) {
        // Constructor without hidden fields (defaults to null)
        public QuestionDTO(Long id, String questionText, List<String> options, String difficulty, Integer pageReference, Integer questionOrder) {
            this(id, questionText, options, difficulty, pageReference, questionOrder, null, null);
        }
    }

    /** Factory for transient QuizDTO used to pass AI-parsed questions before persistence */
    public static QuizDTO withQuestions(List<QuestionDTO> questions) {
        QuizDTO dto = new QuizDTO();
        dto.questions = questions;
        return dto;
    }

    public static QuizDTO from(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.id = quiz.getId();
        dto.documentId = quiz.getDocument().getId();
        dto.documentName = quiz.getDocument().getOriginalFilename();
        dto.difficulty = quiz.getDifficulty();
        dto.questionCount = quiz.getQuestions().size();
        dto.source = quiz.getSource().name();
        dto.createdAt = quiz.getCreatedAt();
        dto.questions = quiz.getQuestions().stream()
            .map(q -> new QuestionDTO(
                q.getId(),
                q.getQuestionText(),
                q.getOptions(),
                q.getDifficulty(),
                q.getPageReference(),
                q.getQuestionOrder()
            ))
            .toList();
        return dto;
    }

    public Long getId() { return id; }
    public Long getDocumentId() { return documentId; }
    public String getDocumentName() { return documentName; }
    public String getDifficulty() { return difficulty; }
    public Integer getQuestionCount() { return questionCount; }
    public String getSource() { return source; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<QuestionDTO> getQuestions() { return questions; }
}
