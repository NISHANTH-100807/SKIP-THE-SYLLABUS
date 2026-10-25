package com.skipthesyllabus.dto;

public class SettingsStatusDTO {
    private boolean aiConfigured;
    private String mode;
    private String modelName;
    private String apiBaseUrl;
    private String setupInstructions;

    public boolean isAiConfigured() { return aiConfigured; }
    public void setAiConfigured(boolean aiConfigured) { this.aiConfigured = aiConfigured; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getApiBaseUrl() { return apiBaseUrl; }
    public void setApiBaseUrl(String apiBaseUrl) { this.apiBaseUrl = apiBaseUrl; }
    public String getSetupInstructions() { return setupInstructions; }
    public void setSetupInstructions(String setupInstructions) { this.setupInstructions = setupInstructions; }
}
