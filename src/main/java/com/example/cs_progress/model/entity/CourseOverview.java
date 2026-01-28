package com.example.cs_progress.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @OneToMany(
            mappedBy = "courseOverview",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<TaskTopicCount> taskTopicCounts = new ArrayList<>();

    @OneToMany(
            mappedBy = "courseOverview",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<TagCount> tagCounts = new HashSet<>();
}
