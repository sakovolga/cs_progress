package com.example.cs_progress.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TopicSummary {

    private String topicTitle;
    private String courseTitle;

    // Метрики по задачам
    private Integer completedTasks;
    private Integer totalTasks;
    private Double taskCompletionPercentage;

    // Метрики по тестам
    private Double bestTestScorePercentage;

    // Активность
    private LocalDateTime lastActivity;
}
