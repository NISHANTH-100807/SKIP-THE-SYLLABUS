package com.skipthesyllabus.controller;

import com.skipthesyllabus.config.XAIConfig;
import com.skipthesyllabus.dto.ApiResponse;
import com.skipthesyllabus.dto.ProgressDTO;
import com.skipthesyllabus.dto.SettingsStatusDTO;
import com.skipthesyllabus.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints for progress tracking, settings status, and health check.
 */
@RestController
@RequestMapping("/api")
public class ProgressController {

    private final ProgressService progressService;
    private final XAIConfig xaiConfig;

    public ProgressController(ProgressService progressService, XAIConfig xaiConfig) {
        this.progressService = progressService;
        this.xaiConfig = xaiConfig;
    }

    /** Learning progress dashboard data */
    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<ProgressDTO>> getProgress() {
        return ResponseEntity.ok(ApiResponse.success(progressService.getProgress()));
    }

    /** AI / settings status */
    @GetMapping({"/settings", "/settings/status"})
    public ResponseEntity<ApiResponse<SettingsStatusDTO>> getSettings() {
        SettingsStatusDTO dto = new SettingsStatusDTO();
        boolean configured = xaiConfig.isConfigured();
        dto.setAiConfigured(configured);
        dto.setMode(configured ? "AI (Grok)" : "Local / Demo");
        dto.setModelName(xaiConfig.getModel());
        dto.setApiBaseUrl(xaiConfig.getBaseUrl());
        if (!configured) {
            dto.setSetupInstructions(
                "Set the XAI_API_KEY environment variable to your xAI API key and restart the application. " +
                "Get your key at https://console.x.ai/");
        }
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /** Health check */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> info = Map.of(
            "status", "UP",
            "application", "Skip The Syllabus",
            "aiMode", xaiConfig.isConfigured() ? "grok" : "local",
            "model", xaiConfig.getModel()
        );
        return ResponseEntity.ok(ApiResponse.success(info));
    }
}
