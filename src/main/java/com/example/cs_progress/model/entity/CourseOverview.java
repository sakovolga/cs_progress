package com.example.cs_progress.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "course_overviews")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CourseOverview extends IdentifiableEntity{

    @Column(name = "course_id", nullable = false)
    private String courseId;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "total_topics")
    private Integer totalTopics;

    @OneToMany(mappedBy = "courseOverview")
    @BatchSize(size = 50)
    @Fetch(FetchMode.SUBSELECT)
    @Builder.Default
    private List<TopicOverview> topicOverviews = new ArrayList<>();

    @OneToMany(mappedBy = "courseOverview")
    @BatchSize(size = 50)
    @Fetch(FetchMode.SUBSELECT)
    @Builder.Default
    private Set<TagCount> tagCounts = new HashSet<>();

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
