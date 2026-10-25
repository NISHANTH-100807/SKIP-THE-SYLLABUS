package com.skipthesyllabus.service.impl;

import com.skipthesyllabus.dto.ExplainRequestDTO;
import com.skipthesyllabus.dto.ExplainResponseDTO;
import com.skipthesyllabus.model.LearningSession;
import com.skipthesyllabus.model.PDFDocument;
import com.skipthesyllabus.repository.LearningSessionRepository;
import com.skipthesyllabus.service.AIService;
import com.skipthesyllabus.service.ExplanationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * COMPOSITION: Uses AIService via dependency injection.
 * Provides fallback when AI is not configured.
 */
@Service
public class ExplanationServiceImpl implements ExplanationService {

    private static final Logger log = LoggerFactory.getLogger(ExplanationServiceImpl.class);

    private final AIService aiService;
    private final LearningSessionRepository sessionRepo;

    public ExplanationServiceImpl(AIService aiService, LearningSessionRepository sessionRepo) {
        this.aiService = aiService;
        this.sessionRepo = sessionRepo;
    }

    @Override
    @Transactional
    public ExplainResponseDTO explain(PDFDocument document, ExplainRequestDTO request) {
        // Log learning session
        LearningSession session = new LearningSession(document, LearningSession.SessionType.EXPLAIN);
        sessionRepo.save(session);

        if (aiService.isConfigured()) {
            try {
                ExplainResponseDTO result = aiService.explain(document, request);
                session.end();
                sessionRepo.save(session);
                return result;
            } catch (Exception e) {
                log.warn("AI explain failed, falling back to local mode: {}", e.getMessage());
            }
        }

        // Local / demo fallback
        return buildLocalFallback(document, request);
    }

    private ExplainResponseDTO buildLocalFallback(PDFDocument document, ExplainRequestDTO request) {
        ExplainResponseDTO dto = new ExplainResponseDTO();
        dto.setConcept(request.getConcept());
        dto.setLevel(request.getLevel());
        dto.setAiGenerated(false);

        String concept = request.getConcept();
        String docName = document.getOriginalFilename();

        dto.setIntuition("You asked about \"" + concept + "\" from " + docName +
            ". To get AI-powered explanations, please configure your Grok API key in the settings. " +
            "In local mode, we can only show you what's in the document text.");

        // Try to find relevant text from the document
        String docText = document.getExtractedText();
        if (docText != null && !docText.isBlank()) {
            String relevant = findRelevantSnippet(docText, concept);
            dto.setBreakdown("Relevant excerpt from the document:\n\n" + relevant);
        } else {
            dto.setBreakdown("The document has no extracted text. Please re-upload or check the PDF.");
        }

        dto.setExample("Configure your XAI_API_KEY environment variable to unlock AI-generated examples.");
        dto.setKeyTakeaways("• Set XAI_API_KEY to enable Grok AI\n• Restart the application after setting the key\n• Visit /api/settings to check configuration status");
        dto.setSelfCheck("Can you locate the term \"" + concept + "\" in the document text?");

        return dto;
    }

    private String findRelevantSnippet(String text, String concept) {
        String lower = text.toLowerCase();
        String conceptLower = concept.toLowerCase();
        int idx = lower.indexOf(conceptLower);
        if (idx == -1) {
            // Return the first 500 chars
            return text.length() > 500 ? text.substring(0, 500) + "..." : text;
        }
        int start = Math.max(0, idx - 200);
        int end = Math.min(text.length(), idx + 600);
        return (start > 0 ? "..." : "") + text.substring(start, end) + (end < text.length() ? "..." : "");
    }
}
