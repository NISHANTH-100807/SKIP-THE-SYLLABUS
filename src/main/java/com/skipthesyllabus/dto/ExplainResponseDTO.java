package com.skipthesyllabus.dto;

import java.util.List;

public class ExplainResponseDTO {
    private String concept;
    private String level;
    private String intuition;
    private String breakdown;
    private String example;
    private String keyTakeaways;
    private String selfCheck;
    private List<Integer> pageReferences;
    private boolean aiGenerated;
    private String rawResponse;

    public String getConcept() { return concept; }
    public void setConcept(String concept) { this.concept = concept; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getIntuition() { return intuition; }
    public void setIntuition(String intuition) { this.intuition = intuition; }
    public String getBreakdown() { return breakdown; }
    public void setBreakdown(String breakdown) { this.breakdown = breakdown; }
    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }
    public String getKeyTakeaways() { return keyTakeaways; }
    public void setKeyTakeaways(String keyTakeaways) { this.keyTakeaways = keyTakeaways; }
    public String getSelfCheck() { return selfCheck; }
    public void setSelfCheck(String selfCheck) { this.selfCheck = selfCheck; }
    public List<Integer> getPageReferences() { return pageReferences; }
    public void setPageReferences(List<Integer> pageReferences) { this.pageReferences = pageReferences; }
    public boolean isAiGenerated() { return aiGenerated; }
    public void setAiGenerated(boolean aiGenerated) { this.aiGenerated = aiGenerated; }
    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
}
