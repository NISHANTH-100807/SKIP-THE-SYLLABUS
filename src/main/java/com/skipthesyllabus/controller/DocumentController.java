package com.skipthesyllabus.controller;

import com.skipthesyllabus.dto.ApiResponse;
import com.skipthesyllabus.dto.DocumentContentDTO;
import com.skipthesyllabus.dto.DocumentDTO;
import com.skipthesyllabus.exception.DocumentNotFoundException;
import com.skipthesyllabus.model.DocumentSection;
import com.skipthesyllabus.model.PDFDocument;
import com.skipthesyllabus.repository.DocumentSectionRepository;
import com.skipthesyllabus.repository.PDFDocumentRepository;
import com.skipthesyllabus.service.DocumentProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for PDF document management.
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final PDFDocumentRepository docRepo;
    private final DocumentSectionRepository sectionRepo;
    private final DocumentProcessor processor;

    public DocumentController(PDFDocumentRepository docRepo,
                              DocumentSectionRepository sectionRepo,
                              DocumentProcessor processor) {
        this.docRepo = docRepo;
        this.sectionRepo = sectionRepo;
        this.processor = processor;
    }

    /** Upload a PDF file */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<DocumentDTO>> upload(
            @RequestParam("file") MultipartFile file) {
        PDFDocument doc = processor.process(file);
        return ResponseEntity.ok(ApiResponse.success(DocumentDTO.from(doc), "Document uploaded and processing started."));
    }

    /** List all documents, newest first */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentDTO>>> list() {
        List<DocumentDTO> docs = docRepo.findAllByOrderByUploadedAtDesc()
            .stream().map(DocumentDTO::from).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(docs));
    }

    /** Search documents by filename */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DocumentDTO>>> search(@RequestParam("q") String query) {
        List<DocumentDTO> docs = docRepo
            .findByOriginalFilenameContainingIgnoreCaseOrderByUploadedAtDesc(query)
            .stream().map(DocumentDTO::from).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(docs));
    }

    /** Get document metadata */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentDTO>> getById(@PathVariable Long id) {
        PDFDocument doc = docRepo.findById(id).orElseThrow(() -> new DocumentNotFoundException(id));
        return ResponseEntity.ok(ApiResponse.success(DocumentDTO.from(doc)));
    }

    /** Get document content (text + sections) */
    @GetMapping("/{id}/content")
    public ResponseEntity<ApiResponse<DocumentContentDTO>> getContent(@PathVariable Long id) {
        PDFDocument doc = docRepo.findById(id).orElseThrow(() -> new DocumentNotFoundException(id));

        DocumentContentDTO content = new DocumentContentDTO();
        content.setDocumentId(doc.getId());
        content.setFilename(doc.getOriginalFilename());
        content.setPageCount(doc.getPageCount());
        content.setFullText(doc.getExtractedText());

        List<DocumentSection> sections = sectionRepo.findByDocumentIdOrderByPageNumberAscSectionIndexAsc(id);
        content.setSections(sections.stream()
            .map(s -> new DocumentContentDTO.SectionDTO(
                s.getId(), s.getPageNumber(), s.getSectionIndex(),
                s.getContent(), s.getTitle(), s.getWordCount()))
            .collect(Collectors.toList()));

        return ResponseEntity.ok(ApiResponse.success(content));
    }

    /** Get document sections list with page references */
    @GetMapping("/{id}/sections")
    public ResponseEntity<ApiResponse<List<DocumentContentDTO.SectionDTO>>> getSections(@PathVariable Long id) {
        if (!docRepo.existsById(id)) {
            throw new DocumentNotFoundException(id);
        }
        List<DocumentSection> sections = sectionRepo.findByDocumentIdOrderByPageNumberAscSectionIndexAsc(id);
        List<DocumentContentDTO.SectionDTO> dtos = sections.stream()
            .map(s -> new DocumentContentDTO.SectionDTO(
                s.getId(), s.getPageNumber(), s.getSectionIndex(),
                s.getContent(), s.getTitle(), s.getWordCount()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /** Delete a document and all related data */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        PDFDocument doc = docRepo.findById(id).orElseThrow(() -> new DocumentNotFoundException(id));
        docRepo.delete(doc);
        return ResponseEntity.ok(ApiResponse.success(null, "Document deleted."));
    }
}
