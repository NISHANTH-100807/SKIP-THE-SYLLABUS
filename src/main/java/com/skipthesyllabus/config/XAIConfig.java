package com.skipthesyllabus.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Holds all xAI API configuration values.
 * Centralises access to avoid scattering @Value annotations throughout the code.
 */
@Component
public class XAIConfig {

    @Value("${xai.api.key:}")
    private String apiKey;

    @Value("${xai.api.base-url:https://api.x.ai/v1}")
    private String baseUrl;

    @Value("${xai.api.model:grok-2-1212}")
    private String model;

    @Value("${xai.api.max-tokens:2048}")
    private int maxTokens;

    @Value("${xai.api.max-context-chars:80000}")
    private int maxContextChars;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String getApiKey() { return apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public String getModel() { return model; }
    public int getMaxTokens() { return maxTokens; }
    public int getMaxContextChars() { return maxContextChars; }

    public String getChatCompletionsUrl() {
        return baseUrl + "/chat/completions";
    }
}
