package com.example.cs_progress.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_course_completion",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCourseCompletion extends IdentifiableEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "course_id", nullable = false)
    private String courseId;

    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Builder.Default
    @Column(name = "celebration_shown", nullable = false)
    private boolean celebrationShown = false;

}