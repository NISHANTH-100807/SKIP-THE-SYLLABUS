package com.skipthesyllabus.controller;

import com.skipthesyllabus.dto.*;
import com.skipthesyllabus.exception.DocumentNotFoundException;
import com.skipthesyllabus.model.PDFDocument;
import com.skipthesyllabus.repository.PDFDocumentRepository;
import com.skipthesyllabus.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for quiz generation, retrieval, and submission.
 */
@RestController
@RequestMapping("/api")
public class QuizController {

    private final PDFDocumentRepository docRepo;
    private final QuizService quizService;

    public QuizController(PDFDocumentRepository docRepo, QuizService quizService) {
        this.docRepo = docRepo;
        this.quizService = quizService;
    }

    /** Generate quiz for a specific document */
    @PostMapping("/documents/{documentId}/quiz")
    public ResponseEntity<ApiResponse<QuizDTO>> generate(
            @PathVariable Long documentId,
            @Valid @RequestBody QuizRequestDTO request) {

        PDFDocument doc = docRepo.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId));

        QuizDTO quiz = quizService.generateQuiz(doc, request);
        return ResponseEntity.ok(ApiResponse.success(quiz, "Quiz generated successfully."));
    }

    /** Get all quizzes for a document */
    @GetMapping("/documents/{documentId}/quizzes")
    public ResponseEntity<ApiResponse<List<QuizDTO>>> listForDocument(@PathVariable Long documentId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizzesForDocument(documentId)));
    }

    /** Get all quizzes across all documents */
    @GetMapping("/quizzes")
    public ResponseEntity<ApiResponse<List<QuizDTO>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(quizService.getAllQuizzes()));
    }

    /** Get quiz attempt history */
    @GetMapping("/quizzes/history")
    public ResponseEntity<ApiResponse<List<QuizAttemptSummaryDTO>>> getHistory() {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizHistory()));
    }

    /** Get a specific quiz by ID */
    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<ApiResponse<QuizDTO>> getQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuiz(quizId)));
    }

    /** Submit quiz answers and get scored result */
    @PostMapping("/quizzes/{quizId}/submit")
    public ResponseEntity<ApiResponse<QuizResultDTO>> submit(
            @PathVariable Long quizId,
            @RequestBody QuizSubmitDTO submission) {

        QuizResultDTO result = quizService.submitQuiz(quizId, submission);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /** Delete a quiz */
    @DeleteMapping("/quizzes/{quizId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.ok(ApiResponse.success(null, "Quiz deleted."));
    }
}
