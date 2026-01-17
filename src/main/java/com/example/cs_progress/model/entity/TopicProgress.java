package com.example.cs_progress.model.entity;

import com.example.cs_common.enums.TopicStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "topic_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "topic_id"}),
        indexes = {
                @Index(name = "idx_user_course", columnList = "user_id, course_id"),
                @Index(name = "idx_user_topic", columnList = "user_id, topic_id"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_last_activity", columnList = "last_activity")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicProgress extends IdentifiableEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "course_id", nullable = false)
    private String courseId;

    @Column(name = "topic_id", nullable = false)
    private String topicId;

    // ========== Агрегированные данные по тестам ==========

//    @Column(name = "total_tests")
//    private Integer totalTests;

//    @Column(name = "completed_tests")
//    @Builder.Default
//    private Integer completedTests = 0;

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "best_test_score_percentage")
    @Builder.Default
    private Double bestTestScorePercentage = 0.0;

//    @Column(name = "avg_test_score")
//    @Builder.Default
//    private Double avgTestScore = 0.0;

    // ========== Агрегированные данные по задачам ==========

    @Column(name = "total_tasks")
    private Integer totalTasks;

    @Column(name = "completed_tasks")
    @Builder.Default
    private Integer completedTasks = 0;

    @Column(name = "task_completion_percentage")
    @Builder.Default
    private Double taskCompletionPercentage = 0.0;

    // ========== Общий прогресс ==========

//    @Column(name = "progress_percentage")
//    @Builder.Default
//    private Integer progressPercentage = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TopicStatus status = TopicStatus.NOT_STARTED;

    // ========== Временные метки ==========

    @Column(name = "first_activity")
    private LocalDateTime firstActivity;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== Lifecycle hooks ==========

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== Бизнес-методы ==========

    /**
     * Обновить прогресс после прохождения теста
     */
//    public void updateFromTestResult(double score) {
//        this.completedTests++;
//
//        // Обновляем лучший балл
//        if (score > this.bestTestScore) {
//            this.bestTestScore = score;
//        }
//
//        // Обновляем средний балл
//        // (можно улучшить, храня сумму и количество)
//        this.avgTestScore = (this.avgTestScore * (this.completedTests - 1) + score) / this.completedTests;
//
//        // Обновляем активность
//        recordActivity();
//
//        // Пересчитываем общий прогресс
//        recalculateProgress();
//    }
//
//    /**
//     * Обновить прогресс после решения задачи
//     */
//    public void incrementTaskCompleted() {
//        this.completedTasks++;
//
//        // Пересчитываем процент выполнения задач
//        this.taskCompletionRate = this.totalTasks != null && this.totalTasks > 0
//                ? (double) this.completedTasks / this.totalTasks
//                : 0.0;
//
//        // Обновляем активность
//        recordActivity();
//
//        // Пересчитываем общий прогресс
//        recalculateProgress();
//    }
//
//    /**
//     * Записать активность
//     */
//    private void recordActivity() {
//        LocalDateTime now = LocalDateTime.now();
//
//        if (this.firstActivity == null) {
//            this.firstActivity = now;
//        }
//
//        this.lastActivity = now;
//    }
//
//    /**
//     * Пересчитать общий прогресс и статус
//     */
//    public void recalculateProgress() {
//        // Прогресс по тестам (50%)
//        double testProgress = totalTests != null && totalTests > 0
//                ? (double) completedTests / totalTests
//                : 0.0;
//
//        // Прогресс по задачам (50%)
//        double taskProgress = totalTasks != null && totalTasks > 0
//                ? (double) completedTasks / totalTasks
//                : 0.0;
//
//        // Общий прогресс
//        this.progressPercentage = (int) ((testProgress * 0.5 + taskProgress * 0.5) * 100);
//
//        // Обновляем статус
//        updateStatus();
//    }
//
//    /**
//     * Обновить статус темы
//     */
//    private void updateStatus() {
//        boolean hasActivity = completedTests > 0 || completedTasks > 0;
//
//        if (!hasActivity) {
//            this.status = TopicStatus.NOT_STARTED;
//            this.completedAt = null;
//            return;
//        }
//
//        // Критерии завершения:
//        // 1. Хотя бы один тест пройден с >= 70%
//        // 2. Решено >= 50% задач
//        boolean testsPassed = bestTestScore >= 70.0;
//        boolean tasksDone = totalTasks != null && totalTasks > 0 &&
//                (double) completedTasks / totalTasks >= 0.5;
//
//        if (testsPassed && tasksDone) {
//            this.status = TopicStatus.COMPLETED;
//            if (this.completedAt == null) {
//                this.completedAt = LocalDateTime.now();
//            }
//        } else {
//            this.status = TopicStatus.IN_PROGRESS;
//            this.completedAt = null;
//        }
//    }
//
//    /**
//     * Установить ожидаемые значения (вызывается при создании)
//     */
//    public void setExpectedValues(Integer totalTests, Integer totalTasks) {
//        this.totalTests = totalTests;
//        this.totalTasks = totalTasks;
//        recalculateProgress();
//    }
//
//    /**
//     * Получить процент прогресса по тестам
//     */
//    public Integer getTestProgressPercentage() {
//        return totalTests != null && totalTests > 0
//                ? (int) ((double) completedTests / totalTests * 100)
//                : 0;
//    }
//
//    /**
//     * Получить процент прогресса по задачам
//     */
//    public Integer getTaskProgressPercentage() {
//        return totalTasks != null && totalTasks > 0
//                ? (int) ((double) completedTasks / totalTasks * 100)
//                : 0;
//    }
//
//    /**
//     * Сколько дней изучает тему
//     */
//    public Long getDaysLearning() {
//        if (firstActivity == null) return 0L;
//        return java.time.temporal.ChronoUnit.DAYS.between(
//                firstActivity.toLocalDate(),
//                LocalDateTime.now().toLocalDate()
//        );
//    }
//
//    /**
//     * Сколько дней с последней активности
//     */
//    public Long getDaysSinceLastActivity() {
//        if (lastActivity == null) return null;
//        return java.time.temporal.ChronoUnit.DAYS.between(
//                lastActivity.toLocalDate(),
//                LocalDateTime.now().toLocalDate()
//        );
//    }
//
//    /**
//     * Проверка на застревание
//     */
//    public boolean isStuck() {
//        Long daysSince = getDaysSinceLastActivity();
//        return status == TopicStatus.IN_PROGRESS &&
//                daysSince != null &&
//                daysSince > 7 &&
//                progressPercentage < 80;
//    }
//
//    /**
//     * Описание прогресса для UI
//     */
//    public String getProgressDescription() {
//        StringBuilder sb = new StringBuilder();
//
//        if (totalTests != null && totalTests > 0) {
//            sb.append(String.format("Тесты: %d/%d (%.0f%%)",
//                    completedTests, totalTests, bestTestScore));
//        }
//
//        if (totalTasks != null && totalTasks > 0) {
//            if (sb.length() > 0) sb.append(" | ");
//            sb.append(String.format("Задачи: %d/%d",
//                    completedTasks, totalTasks));
//        }
//
//        return sb.length() > 0 ? sb.toString() : "Нет активности";
//    }
//
//    // ========== Enum ==========
//
//    public enum TopicStatus {
//        NOT_STARTED,
//        IN_PROGRESS,
//        COMPLETED
//    }
}