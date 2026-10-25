package com.skipthesyllabus.service.impl;

import com.skipthesyllabus.dto.QuizAttemptSummaryDTO;
import com.skipthesyllabus.dto.QuizDTO;
import com.skipthesyllabus.dto.QuizRequestDTO;
import com.skipthesyllabus.dto.QuizResultDTO;
import com.skipthesyllabus.dto.QuizSubmitDTO;
import com.skipthesyllabus.exception.QuizNotFoundException;
import com.skipthesyllabus.model.*;
import com.skipthesyllabus.repository.*;
import com.skipthesyllabus.service.AIService;
import com.skipthesyllabus.service.QuizService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * COMPOSITION: Uses AIService for quiz generation, falls back to local questions.
 * POLYMORPHISM: Works with Question hierarchy via checkAnswer().
 */
@Service
public class QuizServiceImpl implements QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizServiceImpl.class);

    private final AIService aiService;
    private final QuizRepository quizRepo;
    private final MCQQuestionRepository questionRepo;
    private final QuizAttemptRepository attemptRepo;
    private final LearningSessionRepository sessionRepo;
    private final ConceptRepository conceptRepo;

    public QuizServiceImpl(AIService aiService,
                           QuizRepository quizRepo,
                           MCQQuestionRepository questionRepo,
                           QuizAttemptRepository attemptRepo,
                           LearningSessionRepository sessionRepo,
                           ConceptRepository conceptRepo) {
        this.aiService = aiService;
        this.quizRepo = quizRepo;
        this.questionRepo = questionRepo;
        this.attemptRepo = attemptRepo;
        this.sessionRepo = sessionRepo;
        this.conceptRepo = conceptRepo;
    }

    // -------------------------------------------------------------------------
    // Generate Quiz
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public QuizDTO generateQuiz(PDFDocument document, QuizRequestDTO request) {
        Quiz quiz = new Quiz(document, request.getDifficulty(), request.getQuestionCount());
        quiz = quizRepo.save(quiz);

        List<MCQQuestion> questions;

        if (aiService.isConfigured()) {
            try {
                QuizDTO aiResult = aiService.generateQuiz(document, request);
                questions = buildQuestionsFromAI(aiResult, quiz, request);
                quiz.setSource(Quiz.QuizSource.AI_GENERATED);
            } catch (Exception e) {
                log.warn("AI quiz generation failed, using local fallback: {}", e.getMessage());
                questions = buildLocalFallbackQuestions(document, quiz, request);
                quiz.setSource(Quiz.QuizSource.LOCAL_FALLBACK);
            }
        } else {
            questions = buildLocalFallbackQuestions(document, quiz, request);
            quiz.setSource(Quiz.QuizSource.LOCAL_FALLBACK);
        }

        if (questions.isEmpty()) {
            questions = buildLocalFallbackQuestions(document, quiz, request);
            quiz.setSource(Quiz.QuizSource.LOCAL_FALLBACK);
        }

        questionRepo.saveAll(questions);
        quiz.setQuestions(questions);
        quiz.setQuestionCount(questions.size());
        quizRepo.save(quiz);

        // Log session
        sessionRepo.save(new LearningSession(document, LearningSession.SessionType.QUIZ));

        return QuizDTO.from(quiz);
    }

    private List<MCQQuestion> buildQuestionsFromAI(QuizDTO aiResult, Quiz quiz, QuizRequestDTO request) {
        List<MCQQuestion> questions = new ArrayList<>();
        if (aiResult.getQuestions() == null) return questions;

        int order = 1;
        for (QuizDTO.QuestionDTO q : aiResult.getQuestions()) {
            String correctAns = q.correctAnswer() != null ? q.correctAnswer() : "A";
            MCQQuestion mcq = new MCQQuestion(q.questionText(), q.options(), correctAns);
            mcq.setQuiz(quiz);
            mcq.setQuestionOrder(order++);
            mcq.setDifficulty(q.difficulty() != null ? q.difficulty() : request.getDifficulty());
            mcq.setPageReference(q.pageReference());
            mcq.setExplanation(q.explanation() != null ? q.explanation() : "Grounded in uploaded document text.");
            questions.add(mcq);
        }
        return questions;
    }

    private List<MCQQuestion> buildLocalFallbackQuestions(PDFDocument document, Quiz quiz, QuizRequestDTO request) {
        List<MCQQuestion> questions = new ArrayList<>();
        String docName = document.getOriginalFilename();
        int pageCount = document.getPageCount() != null ? document.getPageCount() : 1;

        List<String[]> pool = List.of(
            new String[]{
                "What is the source document for this quiz session?",
                "\"" + docName + "\"", "A random Wikipedia article", "An unverified lecture slide", "General knowledge archive",
                "A", "The quiz is strictly grounded in " + docName + "."
            },
            new String[]{
                "How many pages were successfully indexed from this document?",
                "0 pages", pageCount + " " + (pageCount == 1 ? "page" : "pages"), "Over 5,000 pages", "None (corrupted file)",
                "B", "Apache PDFBox extracted text across " + pageCount + " pages."
            },
            new String[]{
                "In Skip The Syllabus, which format is processed for grounded learning?",
                "Audio recordings (.mp3)", "Executable binaries (.exe)", "PDF documents (.pdf)", "Raw SQL dumps (.sql)",
                "C", "Skip The Syllabus uses Apache PDFBox to parse and index PDF documents."
            },
            new String[]{
                "When Grok AI is configured via XAI_API_KEY, what model is used by default?",
                "Random generator", "Local heuristic matcher", "Traditional keyword lookup", "grok-2-1212 / grok-beta",
                "D", "The platform integrates with xAI Grok (e.g. grok-2-1212) via the official /chat/completions endpoint."
            },
            new String[]{
                "Why are quiz scores verified on the Java backend rather than trusted from client or model?",
                "Backend scoring guarantees deterministic accuracy, tamper resistance, and persistent progress tracking",
                "Because models never make mistakes",
                "To make the application slower",
                "There is no specific reason",
                "A", "Deterministic Java OOP evaluation ensures scoring integrity and prevents prompt hallucination."
            }
        );

        int count = Math.min(request.getQuestionCount(), pool.size());
        for (int i = 0; i < count; i++) {
            String[] item = pool.get(i);
            MCQQuestion mcq = new MCQQuestion(
                item[0],
                List.of(item[1], item[2], item[3], item[4]),
                item[5]
            );
            mcq.setQuiz(quiz);
            mcq.setQuestionOrder(i + 1);
            mcq.setDifficulty(request.getDifficulty());
            mcq.setExplanation(item[6]);
            questions.add(mcq);
        }
        return questions;
    }

    // -------------------------------------------------------------------------
    // Get / List Quizzes
    // -------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public QuizDTO getQuiz(Long quizId) {
        Quiz quiz = quizRepo.findById(quizId)
            .orElseThrow(() -> new QuizNotFoundException(quizId));
        return QuizDTO.from(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDTO> getQuizzesForDocument(Long documentId) {
        return quizRepo.findByDocumentIdOrderByCreatedAtDesc(documentId)
            .stream().map(QuizDTO::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDTO> getAllQuizzes() {
        return quizRepo.findAllByOrderByCreatedAtDesc()
            .stream().map(QuizDTO::from).collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Submit Quiz
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public QuizResultDTO submitQuiz(Long quizId, QuizSubmitDTO submission) {
        Quiz quiz = quizRepo.findById(quizId)
            .orElseThrow(() -> new QuizNotFoundException(quizId));

        QuizAttempt attempt = new QuizAttempt(quiz);
        Map<Long, String> submitted = new HashMap<>();
        Map<Long, Boolean> results = new HashMap<>();

        Map<String, String> answers = submission.getAnswers() != null ? submission.getAnswers() : Map.of();

        for (MCQQuestion q : quiz.getQuestions()) {
            String ans = answers.getOrDefault(String.valueOf(q.getId()), "");
            boolean correct = q.checkAnswer(ans);
            submitted.put(q.getId(), ans);
            results.put(q.getId(), correct);

            // Record concept attempt/weak concept tracking
            recordConceptResult(quiz.getDocument(), q.getQuestionText(), correct);
        }

        attempt.setSubmittedAnswers(submitted);
        attempt.setAnswerResults(results);
        attempt.computeScore();
        attemptRepo.save(attempt);

        return buildResult(attempt, quiz, submitted, results);
    }

    private void recordConceptResult(PDFDocument doc, String questionText, boolean correct) {
        try {
            // Extract a concise concept name from the question text (first 4-6 words)
            String conceptName = questionText.replaceAll("[^a-zA-Z0-9 ]", "").trim();
            String[] words = conceptName.split("\\s+");
            if (words.length > 5) {
                conceptName = String.join(" ", Arrays.copyOfRange(words, 0, 5));
            }
            String name = conceptName.isBlank() ? "General Concept" : conceptName;
            final String finalName = name;

            Concept concept = conceptRepo.findByNameAndDocumentId(finalName, doc.getId())
                .orElseGet(() -> new Concept(finalName, doc));

            concept.recordAttempt(correct);
            conceptRepo.save(concept);
        } catch (Exception e) {
            log.debug("Error recording concept result: {}", e.getMessage());
        }
    }

    private QuizResultDTO buildResult(QuizAttempt attempt, Quiz quiz,
                                      Map<Long, String> submitted, Map<Long, Boolean> results) {
        QuizResultDTO dto = new QuizResultDTO();
        dto.setAttemptId(attempt.getId());
        dto.setQuizId(quiz.getId());
        dto.setTotalQuestions(attempt.getTotalQuestions());
        dto.setCorrectAnswers(attempt.getCorrectAnswers());
        dto.setScorePercent(attempt.getScorePercent());
        dto.setScoreMessage(buildScoreMessage(attempt.getScorePercent()));

        List<QuizResultDTO.QuestionResultDTO> qResults = quiz.getQuestions().stream()
            .map(q -> new QuizResultDTO.QuestionResultDTO(
                q.getId(),
                q.getQuestionText(),
                submitted.getOrDefault(q.getId(), ""),
                q.getCorrectAnswer(),
                Boolean.TRUE.equals(results.get(q.getId())),
                q.getExplanation(),
                q.getOptions()
            )).collect(Collectors.toList());

        dto.setQuestionResults(qResults);
        return dto;
    }

    private String buildScoreMessage(Double score) {
        if (score == null) return "No score computed.";
        if (score >= 90) return "You got " + String.format("%.0f", score) + "%. Outstanding! The syllabus is nervous. 🔥";
        if (score >= 75) return "You got " + String.format("%.0f", score) + "%. Solid work! Brain cells: +1. 💪";
        if (score >= 60) return "You got " + String.format("%.0f", score) + "%. Not bad! More practice and you'll crush it. 📚";
        if (score >= 40) return "You got " + String.format("%.0f", score) + "%. The academic damage is noted. Keep grinding! 🔄";
        return "You got " + String.format("%.0f", score) + "%. Your syllabus cooked you this round. Let's cook back! 🍳";
    }

    // -------------------------------------------------------------------------
    // Delete Quiz
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public void deleteQuiz(Long quizId) {
        if (!quizRepo.existsById(quizId)) throw new QuizNotFoundException(quizId);
        quizRepo.deleteById(quizId);
    }

    // -------------------------------------------------------------------------
    // Quiz History
    // -------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<QuizAttemptSummaryDTO> getQuizHistory() {
        return attemptRepo.findAllByOrderByAttemptedAtDesc().stream()
            .limit(25)
            .map(a -> new QuizAttemptSummaryDTO(
                a.getId(),
                a.getQuiz().getId(),
                a.getQuiz().getDocument() != null ? a.getQuiz().getDocument().getOriginalFilename() : "Quiz #" + a.getQuiz().getId(),
                a.getTotalQuestions(),
                a.getCorrectAnswers(),
                a.getScorePercent(),
                a.getAttemptedAt()
            ))
            .collect(Collectors.toList());
    }
}
