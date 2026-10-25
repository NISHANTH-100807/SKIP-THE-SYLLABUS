package com.skipthesyllabus.service;

import com.skipthesyllabus.dto.ExplainRequestDTO;
import com.skipthesyllabus.dto.ExplainResponseDTO;
import com.skipthesyllabus.model.PDFDocument;

public interface ExplanationService {
    ExplainResponseDTO explain(PDFDocument document, ExplainRequestDTO request);
}
