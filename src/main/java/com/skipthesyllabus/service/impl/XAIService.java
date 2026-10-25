package com.skipthesyllabus.service.impl;

import com.skipthesyllabus.config.XAIConfig;
import com.skipthesyllabus.dto.*;
import com.skipthesyllabus.model.PDFDocument;
import com.skipthesyllabus.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * xAI Grok implementation of AIService.
 * ENCAPSULATION: All API interaction details hidden behind the AIService interface.
 */
@Service
public class XAIService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(XAIService.class);

    private final XAIConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public XAIService(XAIConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean isConfigured() { return config.isConfigured(); }

    @Override
    public String getModelName() { return config.getModel(); }

    // =========================================================================
    // EXPLAIN
    // =========================================================================
    @Override
    public ExplainResponseDTO explain(PDFDocument document, ExplainRequestDTO request) {
        String context = truncateContext(document.getExtractedText());
        String systemPrompt = buildExplainSystemPrompt(request.getLevel());
        String userPrompt = buildExplainUserPrompt(context, request.getConcept(), request.getLevel());

        String raw = callGrok(systemPrompt, userPrompt);

        ExplainResponseDTO dto = parseExplainResponse(raw, request.getConcept(), request.getLevel());
        dto.setAiGenerated(true);
        dto.setRawResponse(raw);
        return dto;
    }

    private String buildExplainSystemPrompt(String level) {
        return """
            You are an expert tutor who explains complex topics clearly.
            Always structure your response EXACTLY as follows (use these exact section headers):

            INTUITION:
            <one-paragraph intuitive explanation>

            BREAKDOWN:
            <step-by-step detailed explanation with numbered points>

            EXAMPLE:
            <concrete real-world example>

            KEY TAKEAWAYS:
            <3-5 bullet points of the most important concepts>

            SELF-CHECK:
            <1-2 questions the student can ask themselves to test understanding>

            Adjust complexity for level: %s (beginner=simple language, intermediate=some jargon ok, advanced=technical depth).
            """.formatted(level);
    }

    private String buildExplainUserPrompt(String context, String concept, String level) {
        return """
            Document context (use this as the primary source):
            ---
            %s
            ---

            Please explain the following concept from this document: "%s"
            Level: %s
            """.formatted(context, concept, level);
    }

    private ExplainResponseDTO parseExplainResponse(String raw, String concept, String level) {
        ExplainResponseDTO dto = new ExplainResponseDTO();
        dto.setConcept(concept);
        dto.setLevel(level);

        dto.setIntuition(extractSection(raw, "INTUITION:", "BREAKDOWN:"));
        dto.setBreakdown(extractSection(raw, "BREAKDOWN:", "EXAMPLE:"));
        dto.setExample(extractSection(raw, "EXAMPLE:", "KEY TAKEAWAYS:"));
        dto.setKeyTakeaways(extractSection(raw, "KEY TAKEAWAYS:", "SELF-CHECK:"));
        dto.setSelfCheck(extractSection(raw, "SELF-CHECK:", null));

        if (dto.getBreakdown() == null || dto.getBreakdown().isBlank()) {
            dto.setBreakdown(raw);
        }
        return dto;
    }

    // =========================================================================
    // QUIZ GENERATION — returns a list of parsed question data
    // =========================================================================
    @Override
    public QuizDTO generateQuiz(PDFDocument document, QuizRequestDTO request) {
        String context = truncateContext(document.getExtractedText());
        String systemPrompt = buildQuizSystemPrompt();
        String userPrompt = buildQuizUserPrompt(context, request.getQuestionCount(), request.getDifficulty());

        String raw = callGrok(systemPrompt, userPrompt);

        // Parse JSON from AI response into a transient QuizDTO carrying question data
        return parseQuizResponse(raw, request);
    }

    private String buildQuizSystemPrompt() {
        return """
            You are a quiz generator. Generate multiple-choice questions based on the provided document.

            Output ONLY a valid JSON array. No markdown, no extra text. Format:
            [
              {
                "question": "Question text here?",
                "options": ["Option A text", "Option B text", "Option C text", "Option D text"],
                "correct": "A",
                "explanation": "Why this answer is correct",
                "difficulty": "easy",
                "pageRef": 1
              }
            ]

            Rules:
            - correct must be exactly "A", "B", "C", or "D"
            - options array must have exactly 4 elements
            - base questions on document content only
            """;
    }

    private String buildQuizUserPrompt(String context, int count, String difficulty) {
        return """
            Document:
            ---
            %s
            ---

            Generate exactly %d multiple-choice questions.
            Difficulty: %s
            """.formatted(context, count, difficulty);
    }

    /**
     * Parses the AI JSON array response into a QuizDTO whose questions list
     * carries the AI-generated question data. The caller (QuizServiceImpl) uses
     * dto.getQuestions() to build persistent MCQQuestion entities.
     */
    private QuizDTO parseQuizResponse(String raw, QuizRequestDTO request) {
        List<QuizDTO.QuestionDTO> questions = new ArrayList<>();

        try {
            // Strip optional markdown code fences
            String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            int start = cleaned.indexOf('[');
            int end = cleaned.lastIndexOf(']');
            if (start == -1 || end == -1) throw new RuntimeException("No JSON array found in AI response");

            String json = cleaned.substring(start, end + 1);
            JsonNode arr = objectMapper.readTree(json);

            for (int i = 0; i < arr.size(); i++) {
                JsonNode q = arr.get(i);
                List<String> opts = new ArrayList<>();
                if (q.has("options")) q.get("options").forEach(o -> opts.add(o.asText()));

                String correct = q.has("correct") ? q.get("correct").asText().trim().toUpperCase() : "A";
                if (!List.of("A", "B", "C", "D").contains(correct)) {
                    correct = "A";
                }
                String expl = q.has("explanation") ? q.get("explanation").asText() : "Based on document context.";

                questions.add(new QuizDTO.QuestionDTO(
                    (long) (i + 1),
                    q.has("question") ? q.get("question").asText() : "Question " + (i + 1),
                    opts,
                    q.has("difficulty") ? q.get("difficulty").asText() : request.getDifficulty(),
                    q.has("pageRef") ? q.get("pageRef").asInt() : null,
                    i + 1,
                    correct,
                    expl
                ));
            }
        } catch (Exception e) {
            log.error("Quiz JSON parse error: {}. Raw length={}", e.getMessage(),
                raw == null ? 0 : raw.length());
            // Return empty list — QuizServiceImpl falls back to local questions
        }

        // Create a transient QuizDTO with the parsed questions embedded
        // QuizServiceImpl reads getQuestions() to persist them
        return QuizDTO.withQuestions(questions);
    }

    // =========================================================================
    // CHAT
    // =========================================================================
    @Override
    public ChatResponseDTO chat(PDFDocument document, ChatRequestDTO request) {
        String context = truncateContext(document.getExtractedText());
        String systemPrompt = buildChatSystemPrompt(context, document.getOriginalFilename());

        List<ObjectNode> messages = new ArrayList<>();

        ObjectNode sys = objectMapper.createObjectNode();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        messages.add(sys);

        if (request.getHistory() != null) {
            for (ChatRequestDTO.ChatMessage h : request.getHistory()) {
                ObjectNode msg = objectMapper.createObjectNode();
                msg.put("role", h.role());
                msg.put("content", h.content());
                messages.add(msg);
            }
        }

        ObjectNode userMsg = objectMapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", request.getMessage());
        messages.add(userMsg);

        String answer = callGrokWithMessages(messages);
        return new ChatResponseDTO(answer, List.of(), true, config.getModel());
    }

    private String buildChatSystemPrompt(String context, String filename) {
        return """
            You are a helpful study assistant for the document: "%s".

            Document content:
            ---
            %s
            ---

            Answer questions based on the document. If something is not covered, say so clearly.
            Be concise, accurate, and helpful. Use examples when relevant.
            """.formatted(filename, context);
    }

    // =========================================================================
    // HTTP helpers
    // =========================================================================
    private String callGrok(String systemPrompt, String userPrompt) {
        List<ObjectNode> messages = new ArrayList<>();
        ObjectNode sys = objectMapper.createObjectNode();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        messages.add(sys);

        ObjectNode user = objectMapper.createObjectNode();
        user.put("role", "user");
        user.put("content", userPrompt);
        messages.add(user);

        return callGrokWithMessages(messages);
    }

    private String callGrokWithMessages(List<ObjectNode> messages) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", config.getModel());
            body.put("max_tokens", config.getMaxTokens());
            body.put("temperature", 0.7);

            ArrayNode msgs = objectMapper.createArrayNode();
            messages.forEach(msgs::add);
            body.set("messages", msgs);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getApiKey());

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                config.getChatCompletionsUrl(), entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("API returned HTTP " + response.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.at("/choices/0/message/content").asText();
            if (content == null || content.isBlank()) {
                throw new RuntimeException("Empty content in API response");
            }
            return content;

        } catch (Exception e) {
            log.error("xAI API call failed: {}", e.getMessage());
            throw new com.skipthesyllabus.exception.AIServiceException(
                "AI service call failed: " + e.getMessage(), "AI_API_ERROR", e);
        }
    }

    // =========================================================================
    // Utilities
    // =========================================================================
    private String truncateContext(String text) {
        if (text == null) return "";
        int max = config.getMaxContextChars();
        return text.length() > max ? text.substring(0, max) + "\n[... content truncated ...]" : text;
    }

    private String extractSection(String text, String startMarker, String endMarker) {
        if (text == null) return "";
        int start = text.indexOf(startMarker);
        if (start == -1) return "";
        start += startMarker.length();
        if (endMarker != null) {
            int end = text.indexOf(endMarker, start);
            return end == -1 ? text.substring(start).trim() : text.substring(start, end).trim();
        }
        return text.substring(start).trim();
    }
}
