package com.example.cs_progress.model.entity;

import com.example.cs_common.enums.CodeQualityRating;
import com.example.cs_common.enums.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskProgress  extends IdentifiableEntity{

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "topic_id", nullable = false)
    private String topicId;

    @Column(name = "course_id", nullable = false)
    private String courseId;

    @Column(name = "task_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskStatus taskStatus = TaskStatus.NOT_STARTED;

    @Column(name = "score")
    @Builder.Default
    private Double score = 0.0;

    @Column(name = "last_snapshot", columnDefinition = "TEXT")
    private String lastSnapshot;

    @Column(name = "code_quality_rating")
    @Enumerated(EnumType.STRING)
    private CodeQualityRating codeQualityRating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
