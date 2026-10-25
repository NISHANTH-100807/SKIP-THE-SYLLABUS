package com.skipthesyllabus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class ChatRequestDTO {
    @NotBlank(message = "Message cannot be empty")
    @Size(max = 2000, message = "Message too long")
    private String message;

    private List<ChatMessage> history;

    public record ChatMessage(String role, String content) {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<ChatMessage> getHistory() { return history; }
    public void setHistory(List<ChatMessage> history) { this.history = history; }
}
