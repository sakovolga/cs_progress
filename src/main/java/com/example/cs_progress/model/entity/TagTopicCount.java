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

import java.util.Objects;

@Entity
@Table(name = "tag_topic_counts")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TagTopicCount extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_count_id", nullable = false)
    private TagCount tagCount;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TagTopicCount that = (TagTopicCount) o;
        return Objects.equals(tagCount, that.tagCount) && Objects.equals(topicId, that.topicId) && Objects.equals(count, that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tagCount, topicId, count);
    }
}

