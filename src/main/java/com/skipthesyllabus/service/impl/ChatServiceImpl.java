package com.skipthesyllabus.service.impl;

import com.skipthesyllabus.dto.ChatRequestDTO;
import com.skipthesyllabus.dto.ChatResponseDTO;
import com.skipthesyllabus.model.LearningSession;
import com.skipthesyllabus.model.PDFDocument;
import com.skipthesyllabus.repository.LearningSessionRepository;
import com.skipthesyllabus.service.AIService;
import com.skipthesyllabus.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * COMPOSITION: Delegates AI work to AIService, provides local fallback.
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final AIService aiService;
    private final LearningSessionRepository sessionRepo;

    public ChatServiceImpl(AIService aiService, LearningSessionRepository sessionRepo) {
        this.aiService = aiService;
        this.sessionRepo = sessionRepo;
    }

    @Override
    @Transactional
    public ChatResponseDTO chat(PDFDocument document, ChatRequestDTO request) {
        sessionRepo.save(new LearningSession(document, LearningSession.SessionType.CHAT));

        if (aiService.isConfigured()) {
            try {
                return aiService.chat(document, request);
            } catch (Exception e) {
                log.warn("AI chat failed, using local fallback: {}", e.getMessage());
            }
        }

        return buildLocalFallback(document, request);
    }

    private ChatResponseDTO buildLocalFallback(PDFDocument document, ChatRequestDTO request) {
        String msg = request.getMessage().toLowerCase();
        String text = document.getExtractedText();

        String answer;
        if (text == null || text.isBlank()) {
            answer = "I can't find any text in this document. Try re-uploading it. Also, configure your XAI_API_KEY to get real AI-powered chat! 🤖";
        } else if (msg.contains("summary") || msg.contains("summarize") || msg.contains("overview")) {
            int len = Math.min(text.length(), 600);
            answer = "Here's the beginning of the document (local mode — no AI):\n\n" + text.substring(0, len) + "...\n\n" +
                "_Configure XAI_API_KEY for a proper AI summary._";
        } else {
            // Search for relevant snippet
            String snippet = findRelevantSnippet(text, request.getMessage());
            answer = "🔍 Found this in the document:\n\n" + snippet +
                "\n\n_This is local search mode. Set XAI_API_KEY for real AI answers._";
        }

        return new ChatResponseDTO(answer, List.of(), false, "local-search");
    }

    private String findRelevantSnippet(String text, String query) {
        String lowerText = text.toLowerCase();
        String[] words = query.toLowerCase().split("\\s+");
        int bestIdx = -1;
        int bestScore = 0;

        for (String word : words) {
            if (word.length() < 3) continue;
            int idx = lowerText.indexOf(word);
            if (idx != -1) {
                int score = word.length();
                if (score > bestScore) {
                    bestScore = score;
                    bestIdx = idx;
                }
            }
        }

        if (bestIdx == -1) {
            int len = Math.min(text.length(), 400);
            return text.substring(0, len) + (text.length() > 400 ? "..." : "");
        }

        int start = Math.max(0, bestIdx - 150);
        int end = Math.min(text.length(), bestIdx + 450);
        return (start > 0 ? "..." : "") + text.substring(start, end) + (end < text.length() ? "..." : "");
    }
}
