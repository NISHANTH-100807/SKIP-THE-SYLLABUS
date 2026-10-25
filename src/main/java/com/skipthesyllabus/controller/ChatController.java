package com.skipthesyllabus.controller;

import com.skipthesyllabus.dto.*;
import com.skipthesyllabus.exception.DocumentNotFoundException;
import com.skipthesyllabus.model.PDFDocument;
import com.skipthesyllabus.repository.PDFDocumentRepository;
import com.skipthesyllabus.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for document-grounded AI chat.
 */
@RestController
@RequestMapping("/api/documents/{documentId}/chat")
public class ChatController {

    private final PDFDocumentRepository docRepo;
    private final ChatService chatService;

    public ChatController(PDFDocumentRepository docRepo, ChatService chatService) {
        this.docRepo = docRepo;
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponseDTO>> chat(
            @PathVariable Long documentId,
            @Valid @RequestBody ChatRequestDTO request) {

        PDFDocument doc = docRepo.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId));

        ChatResponseDTO response = chatService.chat(doc, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
