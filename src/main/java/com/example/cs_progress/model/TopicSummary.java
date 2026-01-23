package com.example.cs_progress.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TopicSummary {

    private String topicId;
    private String topicTitle; // получить из content service или передать
    private String courseId;
    private String courseTitle; // получить из content service или передать

    // Метрики по задачам
    private Integer completedTasks;
    private Integer totalTasks;
    private Double taskCompletionPercentage;

    // Метрики по тестам
    private Double bestTestScorePercentage;

    // Активность
    private LocalDateTime lastActivity;
}
