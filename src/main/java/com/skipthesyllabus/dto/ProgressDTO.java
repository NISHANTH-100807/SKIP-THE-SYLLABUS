package com.skipthesyllabus.dto;

import java.util.List;

public class ProgressDTO {
    private long totalDocuments;
    private long totalQuizzes;
    private long totalAttempts;
    private Double averageScore;
    private long explainSessions;
    private long chatSessions;
    private List<WeakConceptDTO> weakConcepts;
    private List<RecentActivityDTO> recentActivity;

    public record WeakConceptDTO(String name, int incorrectCount, int attemptCount, double errorRate) {}
    public record RecentActivityDTO(String type, String description, String timestamp) {}

    public long getTotalDocuments() { return totalDocuments; }
    public void setTotalDocuments(long totalDocuments) { this.totalDocuments = totalDocuments; }
    public long getTotalQuizzes() { return totalQuizzes; }
    public void setTotalQuizzes(long totalQuizzes) { this.totalQuizzes = totalQuizzes; }
    public long getTotalAttempts() { return totalAttempts; }
    public void setTotalAttempts(long totalAttempts) { this.totalAttempts = totalAttempts; }
    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
    public long getExplainSessions() { return explainSessions; }
    public void setExplainSessions(long explainSessions) { this.explainSessions = explainSessions; }
    public long getChatSessions() { return chatSessions; }
    public void setChatSessions(long chatSessions) { this.chatSessions = chatSessions; }
    public List<WeakConceptDTO> getWeakConcepts() { return weakConcepts; }
    public void setWeakConcepts(List<WeakConceptDTO> weakConcepts) { this.weakConcepts = weakConcepts; }
    public List<RecentActivityDTO> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<RecentActivityDTO> recentActivity) { this.recentActivity = recentActivity; }
}
