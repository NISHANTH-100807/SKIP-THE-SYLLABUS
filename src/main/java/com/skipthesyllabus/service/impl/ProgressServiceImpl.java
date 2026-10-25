package com.skipthesyllabus.service.impl;

import com.skipthesyllabus.dto.ProgressDTO;
import com.skipthesyllabus.model.LearningSession;
import com.skipthesyllabus.model.QuizAttempt;
import com.skipthesyllabus.repository.*;
import com.skipthesyllabus.service.ProgressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProgressServiceImpl implements ProgressService {

    private final PDFDocumentRepository docRepo;
    private final QuizRepository quizRepo;
    private final QuizAttemptRepository attemptRepo;
    private final LearningSessionRepository sessionRepo;
    private final ConceptRepository conceptRepo;

    public ProgressServiceImpl(PDFDocumentRepository docRepo,
                                QuizRepository quizRepo,
                                QuizAttemptRepository attemptRepo,
                                LearningSessionRepository sessionRepo,
                                ConceptRepository conceptRepo) {
        this.docRepo = docRepo;
        this.quizRepo = quizRepo;
        this.attemptRepo = attemptRepo;
        this.sessionRepo = sessionRepo;
        this.conceptRepo = conceptRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public ProgressDTO getProgress() {
        ProgressDTO dto = new ProgressDTO();

        dto.setTotalDocuments(docRepo.count());
        dto.setTotalQuizzes(quizRepo.count());
        dto.setTotalAttempts(attemptRepo.countAllAttempts());
        dto.setAverageScore(attemptRepo.findAverageScore().orElse(null));
        dto.setExplainSessions(sessionRepo.countBySessionType(LearningSession.SessionType.EXPLAIN));
        dto.setChatSessions(sessionRepo.countBySessionType(LearningSession.SessionType.CHAT));

        // Weak concepts
        List<ProgressDTO.WeakConceptDTO> weakConcepts = conceptRepo.findWeakConcepts()
            .stream()
            .limit(5)
            .map(c -> new ProgressDTO.WeakConceptDTO(
                c.getName(), c.getIncorrectCount(), c.getAttemptCount(), c.getErrorRate()))
            .collect(Collectors.toList());
        dto.setWeakConcepts(weakConcepts);

        // Recent activity: combines quiz attempts and study sessions
        List<ProgressDTO.RecentActivityDTO> activity = new ArrayList<>();

        attemptRepo.findAllByOrderByAttemptedAtDesc()
            .stream()
            .limit(6)
            .forEach(a -> activity.add(new ProgressDTO.RecentActivityDTO(
                "QUIZ",
                "Scored " + (a.getScorePercent() != null ? Math.round(a.getScorePercent()) : 0) + "% on " +
                    (a.getQuiz().getDocument() != null ? a.getQuiz().getDocument().getOriginalFilename() : "Quiz #" + a.getQuiz().getId()),
                a.getAttemptedAt().toString()
            )));

        sessionRepo.findAllByOrderByStartedAtDesc()
            .stream()
            .limit(6)
            .forEach(s -> activity.add(new ProgressDTO.RecentActivityDTO(
                s.getSessionType().name(),
                s.getDocument() != null
                    ? s.getSessionType().name() + " session on \"" + s.getDocument().getOriginalFilename() + "\""
                    : s.getSessionType().name() + " session",
                s.getStartedAt().toString()
            )));

        dto.setRecentActivity(activity);
        return dto;
    }
}
