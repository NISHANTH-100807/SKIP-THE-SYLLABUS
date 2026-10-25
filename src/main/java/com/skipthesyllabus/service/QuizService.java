package com.skipthesyllabus.service;

import com.skipthesyllabus.dto.QuizAttemptSummaryDTO;
import com.skipthesyllabus.dto.QuizDTO;
import com.skipthesyllabus.dto.QuizRequestDTO;
import com.skipthesyllabus.dto.QuizResultDTO;
import com.skipthesyllabus.dto.QuizSubmitDTO;
import com.skipthesyllabus.model.PDFDocument;
import java.util.List;

public interface QuizService {
    QuizDTO generateQuiz(PDFDocument document, QuizRequestDTO request);
    QuizDTO getQuiz(Long quizId);
    List<QuizDTO> getQuizzesForDocument(Long documentId);
    List<QuizDTO> getAllQuizzes();
    QuizResultDTO submitQuiz(Long quizId, QuizSubmitDTO submission);
    void deleteQuiz(Long quizId);
    List<QuizAttemptSummaryDTO> getQuizHistory();
}
