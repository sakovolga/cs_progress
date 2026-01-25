package com.example.cs_progress.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_topic_counts")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TaskTopicCount extends IdentifiableEntity {

    @Column(name = "course_id")
    private String courseId;

    @Column(name = "topic_id")
    private String topicId;

    @Column(name = "count")
    @Builder.Default
    private Integer count = 0;

    public void incrementCount() {
        this.count += 1;
    }

    public void decrementCount() {
        if (this.count > 0) {
            this.count -= 1;
        }
    }

}
