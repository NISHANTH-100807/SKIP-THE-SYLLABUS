package com.skipthesyllabus;

import com.skipthesyllabus.model.MCQQuestion;
import com.skipthesyllabus.model.PDFDocument;
import com.skipthesyllabus.model.Quiz;
import com.skipthesyllabus.model.QuizAttempt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration and unit tests for Skip The Syllabus.
 * OOP Concepts demonstrated: polymorphism (checkAnswer), encapsulation, inheritance.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "xai.api.key=",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SkipTheSyllabusApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring context starts correctly even without XAI_API_KEY
    }

    // -------------------------------------------------------------------------
    // Domain Model Tests — Demonstrates OOP
    // -------------------------------------------------------------------------

    @Test
    void pdfDocument_statusTransitions() {
        PDFDocument doc = new PDFDocument("test.pdf", "test.pdf", 1024L);
        assertEquals(PDFDocument.ProcessingStatus.UPLOADED, doc.getStatus());

        doc.markAsProcessing();
        assertEquals(PDFDocument.ProcessingStatus.PROCESSING, doc.getStatus());

        doc.markAsReady("Sample text content", 5);
        assertEquals(PDFDocument.ProcessingStatus.READY, doc.getStatus());
        assertTrue(doc.isReady());
        assertEquals(5, doc.getPageCount());
    }

    @Test
    void pdfDocument_markAsReady_withEmptyText_setsEmptyTextStatus() {
        PDFDocument doc = new PDFDocument("empty.pdf", "empty.pdf", 512L);
        doc.markAsReady("", 2);
        assertEquals(PDFDocument.ProcessingStatus.EMPTY_TEXT, doc.getStatus());
        assertFalse(doc.isReady());
    }

    @Test
    void pdfDocument_markAsFailed() {
        PDFDocument doc = new PDFDocument("bad.pdf", "bad.pdf", 256L);
        doc.markAsFailed();
        assertEquals(PDFDocument.ProcessingStatus.FAILED, doc.getStatus());
        assertNotNull(doc.getProcessedAt());
    }

    /**
     * POLYMORPHISM: MCQQuestion.checkAnswer() called through Question abstraction.
     */
    @Test
    void mcqQuestion_checkAnswer_caseInsensitive() {
        MCQQuestion q = new MCQQuestion(
            "What is 2+2?",
            List.of("3", "4", "5", "6"),
            "B"
        );
        assertTrue(q.checkAnswer("B"));
        assertTrue(q.checkAnswer("b")); // case-insensitive
        assertFalse(q.checkAnswer("A"));
        assertFalse(q.checkAnswer(""));
        assertFalse(q.checkAnswer(null));
        assertEquals("MCQ", q.getQuestionType());
    }

    @Test
    void mcqQuestion_checkAnswer_withWhitespace() {
        MCQQuestion q = new MCQQuestion("Q?", List.of("A", "B", "C", "D"), "C");
        assertTrue(q.checkAnswer("  C  ")); // trims whitespace
    }

    /**
     * INHERITANCE: QuizAttempt uses computeScore() with Map from MCQQuestion IDs.
     */
    @Test
    void quizAttempt_computeScore_correctCalculation() {
        PDFDocument doc = new PDFDocument("doc.pdf", "doc.pdf", 1000L);
        Quiz quiz = new Quiz(doc, "medium", 4);

        // Simulate 4 questions, 3 correct
        MCQQuestion q1 = new MCQQuestion("Q1?", List.of("A","B","C","D"), "A"); q1.setId(1L); q1.setQuiz(quiz); q1.setQuestionOrder(1);
        MCQQuestion q2 = new MCQQuestion("Q2?", List.of("A","B","C","D"), "B"); q2.setId(2L); q2.setQuiz(quiz); q2.setQuestionOrder(2);
        MCQQuestion q3 = new MCQQuestion("Q3?", List.of("A","B","C","D"), "C"); q3.setId(3L); q3.setQuiz(quiz); q3.setQuestionOrder(3);
        MCQQuestion q4 = new MCQQuestion("Q4?", List.of("A","B","C","D"), "D"); q4.setId(4L); q4.setQuiz(quiz); q4.setQuestionOrder(4);
        quiz.setQuestions(List.of(q1, q2, q3, q4));

        QuizAttempt attempt = new QuizAttempt(quiz);
        attempt.setAnswerResults(Map.of(1L, true, 2L, true, 3L, true, 4L, false));
        attempt.computeScore();

        assertEquals(3, attempt.getCorrectAnswers());
        assertEquals(4, attempt.getTotalQuestions());
        assertEquals(75.0, attempt.getScorePercent(), 0.01);
    }

    @Test
    void quizAttempt_computeScore_allWrong() {
        PDFDocument doc = new PDFDocument("doc.pdf", "doc.pdf", 1000L);
        Quiz quiz = new Quiz(doc, "hard", 2);
        MCQQuestion q1 = new MCQQuestion("Q1?", List.of("A","B","C","D"), "A"); q1.setId(1L); q1.setQuiz(quiz);
        MCQQuestion q2 = new MCQQuestion("Q2?", List.of("A","B","C","D"), "B"); q2.setId(2L); q2.setQuiz(quiz);
        quiz.setQuestions(List.of(q1, q2));

        QuizAttempt attempt = new QuizAttempt(quiz);
        attempt.setAnswerResults(Map.of(1L, false, 2L, false));
        attempt.computeScore();

        assertEquals(0, attempt.getCorrectAnswers());
        assertEquals(0.0, attempt.getScorePercent(), 0.01);
    }

    @Test
    void quiz_sourceEnum_defaultIsAiGenerated() {
        PDFDocument doc = new PDFDocument("doc.pdf", "doc.pdf", 1000L);
        Quiz quiz = new Quiz(doc, "easy", 5);
        assertEquals(Quiz.QuizSource.AI_GENERATED, quiz.getSource());
    }
}
