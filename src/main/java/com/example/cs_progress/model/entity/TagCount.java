package com.example.cs_progress.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "tag_counts")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TagCount extends IdentifiableEntity {

    @Column(name = "tag_name")
    private String tagName;

    @Column(name = "course_id")
    private String courseId;

    @OneToMany(
            mappedBy = "tagCount",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<TagTopicCount> topicCounts = new ArrayList<>();

    /**
     * Производное поле — НЕ хранится в БД
     */
    @Transient
    public Integer getCount() {
        return topicCounts.stream()
                .mapToInt(TagTopicCount::getCount)
                .sum();
    }

}

