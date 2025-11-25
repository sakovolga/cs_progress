package com.example.cs_progress.model.entity;

import com.example.cs_common.enums.Difficulty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "test_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResult extends IdentifiableEntity{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", referencedColumnName = "id")
    private Quiz quiz;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "test_id", nullable = false)
    private String testId;

    @Column(name = "difficulty")
    private Difficulty difficulty;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "course_id", nullable = false)
    private String courseId;

    @Column(name = "topic_id", nullable = false)
    private String topicId;
}
