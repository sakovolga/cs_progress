package com.example.cs_progress.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PromptData {

    private String userId;

    // ========== Топики ==========

    /**
     * Топ-3 топика с лучшими результатами по задачам
     * Сортировка: taskCompletionPercentage DESC, lastActivity DESC
     */
    private List<TopicSummary> bestTopicsByTasks;

    /**
     * Топ-3 топика с худшими результатами по задачам
     * Сортировка: taskCompletionPercentage ASC, lastActivity DESC
     */
    private List<TopicSummary> worstTopicsByTasks;

    /**
     * Топ-3 топика с лучшими результатами по тестам
     * Сортировка: bestTestScorePercentage DESC, lastActivity DESC
     */
    private List<TopicSummary> bestTopicsByTests;

    /**
     * Топ-3 топика с худшими результатами по тестам
     * Сортировка: bestTestScorePercentage ASC, lastActivity DESC
     */
    private List<TopicSummary> worstTopicsByTests;

    // ========== Навыки ==========

    /**
     * Топ-3 лучших навыка в контексте топиков
     * Сортировка: progressInTopic DESC, topicLastActivity DESC
     */
    private List<SkillSummary> bestSkills;

    /**
     * Топ-3 слабых навыка в контексте топиков
     * Сортировка: progressInTopic ASC, topicLastActivity DESC
     */
    private List<SkillSummary> weakSkills;

    // ========== Активность ==========

    /**
     * Дней с последней активности
     */
    private Integer daysSinceLastActivity;

    /**
     * Текущий streak (опционально)
     */
    private Integer currentStreak;
}
