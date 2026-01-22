package com.example.cs_progress.model.entity;

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
@Table(name = "tag_task_topic_counts")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TagTaskTopicCount extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_task_count_id", nullable = false)
    private TagTaskCount tagTaskCount;

    @Column(name = "topic_id")
    private String topicId;

    @Column(name = "count")
    private Integer count;

    public void incrementCount() {
        if (this.count == null) {
            this.count = 0;
        }
        this.count++;
    }

    public void decrementCount() {
        if (this.count == null || this.count <= 0) {
            this.count = 0;
        } else {
            this.count--;
        }
    }
}

