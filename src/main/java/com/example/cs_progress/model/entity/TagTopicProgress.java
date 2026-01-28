package com.example.cs_progress.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "tag_topic_progresses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tag_progress_id", "topic_id"}),
        indexes = {
                @Index(name = "idx_topic_id", columnList = "topic_id")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagTopicProgress extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_progress_id", referencedColumnName = "id")
    private TagProgress tagProgress;

    @Column(name = "topic_id", nullable = false)
    @Setter
    private String topicId;

    // ========== ТЕСТЫ (без "ожидаемого") ==========

    @Column(name = "test_questions_answered")
    @Builder.Default
    private Integer testQuestionsAnswered = 0;   // сколько вопросов ответил

    @Column(name = "correct_test_answers")
    @Builder.Default
    private Integer correctTestAnswers = 0;    // сколько правильно

    @Column(name = "test_success_rate")
    @Builder.Default
    private Double testSuccessRate = 0.0;        // correct / answered

    // ========== ЗАДАЧИ (с "ожидаемым") ==========

    @Column(name = "tasks_completed")
    @Builder.Default
    private Integer tasksCompleted = 0;

    @Column(name = "expected_tasks")
    @Setter
    private Integer expectedTasks;               // ✅ Есть expected

    @Column(name = "task_completion_rate")
    @Builder.Default
    private Double taskCompletionRate = 0.0;     // completed / expected

    // ========== ОБЩИЙ ПРОГРЕСС ==========

    @Column(name = "progress_in_topic")
    @Builder.Default
    private Double progressInTopic = 0.0;

    // ========== Технические метки ==========

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

    public void incrementCorrectTestAnswers() {
        this.testQuestionsAnswered++;
        this.correctTestAnswers++;
        recalculateTestMetrics();

    }

    public void incrementQuestionsAnswered() {
        this.testQuestionsAnswered++;
        recalculateTestMetrics();
    }

    private void recalculateTestMetrics() {
        this.testSuccessRate = testQuestionsAnswered > 0
                ? (double) correctTestAnswers / testQuestionsAnswered * 100
                : 0.0;
        calculateOverallProgress();
    }





//    public void updateFromTestResult(int totalQuestions, int correctAnswers) {
//        this.testQuestionsAnswered += totalQuestions;
//        this.testQuestionsCorrect += correctAnswers;
//        recalculateMetrics();
//    }

    public void incrementTaskCompleted() {
        this.tasksCompleted++;
        recalculateTaskMetrics();
    }

    private void recalculateTaskMetrics() {
        this.taskCompletionRate = expectedTasks > 0 ?
                (double) tasksCompleted / expectedTasks * 100
                : 0.0;
        calculateOverallProgress();
    }
//
//    public void setExpectedTasksIfNull(Integer expected) {
//        if (this.expectedTasks == null) {
//            this.expectedTasks = expected;
//            recalculateMetrics();
//        }
//    }

//    public void recalculateMetrics() {
//        this.testSuccessRate = testQuestionsAnswered > 0
//                ? (double) testQuestionsCorrect / testQuestionsAnswered
//                : 0.0;
//
//        this.taskCompletionRate = expectedTasks != null && expectedTasks > 0
//                ? (double) tasksCompleted / expectedTasks
//                : 0.0;
//
//        this.progressInTopic = calculateOverallProgress();
//    }

    private void calculateOverallProgress() {
        boolean hasTestData = testQuestionsAnswered > 0;
        boolean hasTaskData = expectedTasks != null && expectedTasks > 0;

        if (hasTestData && hasTaskData) {
            progressInTopic = testSuccessRate * 0.5
                    + taskCompletionRate * 0.5;
        } else if (hasTestData) {
            progressInTopic = testSuccessRate;
        } else if (hasTaskData) {
            progressInTopic = taskCompletionRate;
        } else {
            progressInTopic = 0.0;
        }
    }

    public boolean isTopicCompleted() {
        return progressInTopic != null && progressInTopic >= 0.8;
    }

    public boolean hasActivity() {
        return tasksCompleted > 0 || testQuestionsAnswered > 0;
    }

    public Integer getProgressPercentage() {
        return progressInTopic != null ? (int) Math.round(progressInTopic * 100) : 0;
    }

    public Integer getRemainingTasks() {
        if (expectedTasks == null) return null;
        return Math.max(0, expectedTasks - tasksCompleted);
    }

//    public String getProgressDescription() {
//        StringBuilder sb = new StringBuilder();
//
//        if (expectedTasks != null && expectedTasks > 0) {
//            sb.append(String.format("%d/%d задач", tasksCompleted, expectedTasks));
//        }
//
//        if (testQuestionsAnswered > 0) {
//            if (sb.length() > 0) sb.append(", ");
//            sb.append(String.format("%d%% правильных ответов",
//                    (int)(testSuccessRate * 100)));
//        }
//
//        return sb.length() > 0 ? sb.toString() : "Нет активности";
//    }


}