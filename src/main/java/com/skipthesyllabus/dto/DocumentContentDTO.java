package com.skipthesyllabus.dto;

import java.util.List;

public class DocumentContentDTO {
    private Long documentId;
    private String filename;
    private Integer pageCount;
    private String fullText;
    private List<SectionDTO> sections;

    public record SectionDTO(Long id, Integer pageNumber, Integer sectionIndex, String content, String title, Integer wordCount) {}

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    public String getFullText() { return fullText; }
    public void setFullText(String fullText) { this.fullText = fullText; }
    public List<SectionDTO> getSections() { return sections; }
    public void setSections(List<SectionDTO> sections) { this.sections = sections; }
}
