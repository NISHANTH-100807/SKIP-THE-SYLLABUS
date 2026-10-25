package com.skipthesyllabus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ExplainRequestDTO {
    @NotBlank(message = "Concept or question cannot be blank")
    @Size(max = 500, message = "Concept must be under 500 characters")
    private String concept;

    private String level = "intermediate";

    private Integer sectionId;

    public String getConcept() { return concept; }
    public void setConcept(String concept) { this.concept = concept; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Integer getSectionId() { return sectionId; }
    public void setSectionId(Integer sectionId) { this.sectionId = sectionId; }
}
