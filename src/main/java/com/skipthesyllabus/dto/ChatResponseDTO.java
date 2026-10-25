package com.skipthesyllabus.dto;

import java.util.List;

public class ChatResponseDTO {
    private String answer;
    private List<Integer> pageReferences;
    private boolean aiGenerated;
    private String modelUsed;

    public ChatResponseDTO() {}

    public ChatResponseDTO(String answer, List<Integer> pageReferences, boolean aiGenerated, String modelUsed) {
        this.answer = answer;
        this.pageReferences = pageReferences;
        this.aiGenerated = aiGenerated;
        this.modelUsed = modelUsed;
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public List<Integer> getPageReferences() { return pageReferences; }
    public void setPageReferences(List<Integer> pageReferences) { this.pageReferences = pageReferences; }
    public boolean isAiGenerated() { return aiGenerated; }
    public void setAiGenerated(boolean aiGenerated) { this.aiGenerated = aiGenerated; }
    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }
}
