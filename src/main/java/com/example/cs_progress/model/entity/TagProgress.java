package com.example.cs_progress.model.entity;

import com.example.cs_common.enums.CompletionType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tag_progress")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TagProgress extends IdentifiableEntity{

    private String userId;
    private String tagName;
    private String courseId;

    // ========== Для дашборда (базовые метрики) ==========

    private Integer totalTasks;
    private Integer resolvedTasks;
    private Integer answeredTestQuestions;
    private Integer correctTestAnswers;
    private Double strengthScore; // вычислять средний процент тасок и тестов с учетом количества тем, чтобы пользователь не пропускал тесты

    // ========== Завершение ==========

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completion_type")
    @Enumerated(EnumType.STRING)
    private CompletionType completionType;

// ========== Временные метки ==========

    @Column(name = "first_activity")
    private LocalDateTime firstActivity;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    // ========== Для рекомендаций ИИ (детализация) ==========

    @OneToMany(mappedBy = "tagProgress", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TagTopicProgress> topicProgresses = new ArrayList<>();

    // ========== Lifecycle hooks ==========

    @PrePersist
    protected void onCreate() {
        if (firstActivity == null) {
            firstActivity = LocalDateTime.now();
        }
        lastActivity = LocalDateTime.now();
    }

    // ========== Бизнес-методы ==========

    /**
     * Записать активность пользователя
     */
    public void recordActivity() {
        this.lastActivity = LocalDateTime.now();
    }

    /**
     * Пересчитать силу навыка
     */
    public void recalculateStrengthScore() {
        double testScore = answeredTestQuestions > 0
                ? (double) correctTestAnswers / answeredTestQuestions
                : 0.0;

        double taskScore = totalTasks > 0
                ? (double) resolvedTasks / totalTasks
                : 0.0;

        // Если только один тип активности - максимум 70%
        if (answeredTestQuestions == 0 && totalTasks > 0) {
            this.strengthScore = Math.min(0.7, taskScore);
            return;
        }

        if (totalTasks == 0 && answeredTestQuestions > 0) {
            this.strengthScore = Math.min(0.7, testScore);
            return;
        }

        // Если оба - полный расчет
        if (answeredTestQuestions > 0 && totalTasks > 0) {
            this.strengthScore = (testScore * 0.5) + (taskScore * 0.5);
            return;
        }

        // Ничего не делал
        this.strengthScore = 0.0;
    }

    /**
     * Проверить и автоматически завершить навык
     */
    public void checkAndAutoComplete() {
        if (isCompleted) return;

        if (meetsCompletionCriteria()) {
            this.isCompleted = true;
            this.completedAt = LocalDateTime.now();
            this.completionType = CompletionType.AUTO;
        }
    }

    /**
     * Отметить как завершенное вручную
     */
    public boolean markAsCompletedManually() {
        if (isCompleted) return false;
        if (strengthScore == null || strengthScore < 0.6) return false;

        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
        this.completionType = CompletionType.MANUAL;
        return true;
    }

    /**
     * Критерии автозавершения
     */
    private boolean meetsCompletionCriteria() {
        boolean strongEnough = strengthScore != null && strengthScore >= 0.85;

        boolean hasCompletedTopics = topicProgresses.stream()
                .anyMatch(TagTopicProgress::isTopicCompleted);

        double testQuality = answeredTestQuestions > 0
                ? (double) correctTestAnswers / answeredTestQuestions
                : 0.0;
        boolean testsGood = answeredTestQuestions > 0 && testQuality >= 0.8;

        return strongEnough && hasCompletedTopics && testsGood;
    }

    /**
     * Получить или создать прогресс для темы
     */
    public TagTopicProgress getOrCreateTopicProgress(String topicId) {
        return topicProgresses.stream()
                .filter(tp -> tp.getTopicId().equals(topicId))
                .findFirst()
                .orElseGet(() -> {
                    TagTopicProgress newTopic = TagTopicProgress.builder()
                            .tagProgress(this)
                            .topicId(topicId)
                            .build();
                    topicProgresses.add(newTopic);
                    return newTopic;
                });
    }

    /**
     * Получить все ID тем, где встречался тег
     */
    public Set<String> getSourceTopicIds() {
        Set<String> topicIds = new HashSet<>();
        for (TagTopicProgress tp : topicProgresses) {
            topicIds.add(tp.getTopicId());
        }
        return topicIds;
    }

    /**
     * Нужно ли повторить навык
     */
    public boolean needsReview() {
        return strengthScore != null && strengthScore < 0.6;
    }

    /**
     * Процент силы для UI
     */
    public Integer getStrengthPercentage() {
        return strengthScore != null ? (int) Math.round(strengthScore * 100) : 0;
    }

    /**
     * Сколько дней изучает навык
     */
    public Long getDaysLearning() {
        if (firstActivity == null) return 0L;
        return ChronoUnit.DAYS.between(firstActivity.toLocalDate(), LocalDateTime.now().toLocalDate());
    }

    /**
     * Сколько дней с последней активности
     */
    public Long getDaysSinceLastActivity() {
        if (lastActivity == null) return null;
        return ChronoUnit.DAYS.between(lastActivity.toLocalDate(), LocalDateTime.now().toLocalDate());
    }

    /**
     * Предупреждение о силе навыка
     */
    public String getStrengthWarning() {
        if (answeredTestQuestions == 0 && totalTasks > 0) {
            return "⚠️ Пройдите тест для увеличения силы навыка";
        }

        if (totalTasks == 0 && answeredTestQuestions > 0) {
            return "⚠️ Решите задачи для закрепления навыка";
        }

        if (answeredTestQuestions == 0 && totalTasks == 0) {
            return "Начните с теста или решите задачу";
        }

        return null;
    }
}
