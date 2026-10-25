package com.skipthesyllabus.service;

import com.skipthesyllabus.dto.ChatRequestDTO;
import com.skipthesyllabus.dto.ChatResponseDTO;
import com.skipthesyllabus.dto.ExplainRequestDTO;
import com.skipthesyllabus.dto.ExplainResponseDTO;
import com.skipthesyllabus.dto.QuizDTO;
import com.skipthesyllabus.dto.QuizRequestDTO;
import com.skipthesyllabus.model.PDFDocument;

// ABSTRACTION - interface hiding implementation details
public interface AIService {
    boolean isConfigured();
    String getModelName();
    ExplainResponseDTO explain(PDFDocument document, ExplainRequestDTO request);
    QuizDTO generateQuiz(PDFDocument document, QuizRequestDTO request);
    ChatResponseDTO chat(PDFDocument document, ChatRequestDTO request);
}
