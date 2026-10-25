package com.skipthesyllabus.controller;

import com.skipthesyllabus.dto.*;
import com.skipthesyllabus.exception.DocumentNotFoundException;
import com.skipthesyllabus.model.PDFDocument;
import com.skipthesyllabus.repository.PDFDocumentRepository;
import com.skipthesyllabus.service.ExplanationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for AI-powered concept explanations.
 */
@RestController
@RequestMapping("/api/documents/{documentId}/explain")
public class ExplainController {

    private final PDFDocumentRepository docRepo;
    private final ExplanationService explanationService;

    public ExplainController(PDFDocumentRepository docRepo, ExplanationService explanationService) {
        this.docRepo = docRepo;
        this.explanationService = explanationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExplainResponseDTO>> explain(
            @PathVariable Long documentId,
            @Valid @RequestBody ExplainRequestDTO request) {

        PDFDocument doc = docRepo.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId));

        ExplainResponseDTO result = explanationService.explain(doc, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
