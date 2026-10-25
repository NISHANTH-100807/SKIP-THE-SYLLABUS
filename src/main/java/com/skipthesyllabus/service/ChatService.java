package com.skipthesyllabus.service;

import com.skipthesyllabus.dto.ChatRequestDTO;
import com.skipthesyllabus.dto.ChatResponseDTO;
import com.skipthesyllabus.model.PDFDocument;

public interface ChatService {
    ChatResponseDTO chat(PDFDocument document, ChatRequestDTO request);
}
